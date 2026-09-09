package com.jmirving.prodata.download;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PackagedWorkerIntegrationTest {
    private static final String FILE_ID = "local-file-2026";
    private static final String FILE_NAME = "2026_LoL_esports_match_data_from_OraclesElixir.csv";
    private static final String CSV = String.join(",",
            "gameid", "league", "split", "year", "date", "game", "patch", "participantid", "side",
            "teamid", "ban1", "ban2", "ban3", "ban4", "ban5", "pick1", "pick2", "pick3", "pick4", "pick5")
            + "\n1,LCS,Spring,2026,2026-01-15,1,15.1,100,Blue,1,A,B,C,D,E,F,G,H,I,J\n";

    @TempDir
    Path tempDir;

    @Test
    void packagedWorkerDownloadsFromConfiguredLocalEndpointAndIsRepeatSafe() throws Exception {
        AtomicInteger listingRequests = new AtomicInteger();
        AtomicInteger downloadRequests = new AtomicInteger();
        AtomicReference<String> downloadQuery = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/folder", exchange -> {
            listingRequests.incrementAndGet();
            respond(exchange, "text/html", folderListing());
        });
        server.createContext("/download", exchange -> {
            downloadRequests.incrementAndGet();
            downloadQuery.set(exchange.getRequestURI().getRawQuery());
            respond(exchange, "text/csv", CSV);
        });
        server.start();

        try {
            int port = server.getAddress().getPort();
            String folderUrl = "http://127.0.0.1:" + port + "/folder";
            String downloadUrl = "http://127.0.0.1:" + port + "/download";
            Path outputDir = tempDir.resolve("caller-owned-output");

            JsonNode first = runWorker(folderUrl, downloadUrl, outputDir);
            Path published = outputDir.resolve(FILE_NAME);
            assertEquals(CSV, Files.readString(published));
            assertMetadata(first, outputDir, downloadUrl);

            JsonNode second = runWorker(folderUrl, downloadUrl, outputDir);
            assertEquals(CSV, Files.readString(published));
            assertMetadata(second, outputDir, downloadUrl);
            assertEquals(2, listingRequests.get());
            assertEquals(2, downloadRequests.get());
            assertEquals("export=download&id=" + FILE_ID, downloadQuery.get());
            try (var temporaryFiles = Files.list(outputDir.resolve("tmp"))) {
                assertFalse(temporaryFiles.anyMatch(Files::isRegularFile));
            }
        } finally {
            server.stop(0);
        }
    }

    private JsonNode runWorker(String folderUrl, String downloadUrl, Path outputDir) throws Exception {
        String javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String jar = System.getProperty("packagedWorkerJar");
        Process process = new ProcessBuilder(
                javaExecutable,
                "-jar",
                jar,
                "--prodata.download.googleDriveFolderUrl=" + folderUrl,
                "--prodata.download.googleDriveDownloadUrl=" + downloadUrl,
                "--prodata.download.outputDir=" + outputDir,
                "--prodata.download.years=2026",
                "--prodata.download.structuredOutput=json"
        ).start();

        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            fail("packaged worker timed out");
        }
        String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, process.exitValue(), () -> "worker failed: " + stderr + stdout);
        return new ObjectMapper().readTree(stdout);
    }

    private void assertMetadata(JsonNode result, Path outputDir, String downloadUrl) throws Exception {
        assertEquals("SUCCESS", result.get("status").asText());
        JsonNode metadata = result.get("metadata");
        assertEquals(outputDir.toAbsolutePath().toString(), metadata.get("outputDirectory").asText());
        assertEquals(2026, metadata.get("years").get(0).asInt());
        JsonNode artifact = metadata.get("artifacts").get(0);
        assertEquals(FILE_NAME, artifact.get("fileName").asText());
        assertEquals(outputDir.resolve(FILE_NAME).toAbsolutePath().toString(), artifact.get("path").asText());
        assertEquals(1, artifact.get("rowCount").asLong());
        assertEquals(sha256(CSV), artifact.get("sha256").asText());
        assertEquals(downloadUrl + "?export=download&id=" + FILE_ID, artifact.get("sourceUrl").asText());
        assertTrue(artifact.hasNonNull("generatedAt"));
        assertFalse(artifact.has("manifestPath"));
    }

    private String folderListing() {
        return "[[null,\"" + FILE_ID + "\"],null,null,null,\"text/csv\",\"" + FILE_NAME + "\"]";
    }

    private String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    private void respond(HttpExchange exchange, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
