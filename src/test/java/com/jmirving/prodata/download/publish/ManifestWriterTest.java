package com.jmirving.prodata.download.publish;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ManifestWriterTest {
    @TempDir
    Path tempDir;

    @Test
    void writesManifestWithChecksumAndRowCount() throws IOException, NoSuchAlgorithmException {
        Path csv = tempDir.resolve("2026_LoL_esports_match_data_from_OraclesElixir.csv");
        Files.writeString(csv, "a,b\n1,2\n3,4\n");

        Path manifestPath = tempDir.resolve("2026_LoL_esports_match_data_from_OraclesElixir.csv.manifest.json");
        String sourceUrl = "https://example.com/source.csv";

        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        ManifestWriter writer = new ManifestWriter(objectMapper,
                Clock.fixed(Instant.parse("2026-01-08T12:00:00Z"), ZoneOffset.UTC));
        DownloadManifest manifest = writer.write(csv, manifestPath, sourceUrl);

        DownloadManifest persisted = objectMapper.readValue(manifestPath.toFile(), DownloadManifest.class);

        assertNotNull(manifest.generatedAt());
        assertEquals(2, manifest.rowCount());
        assertEquals(sourceUrl, manifest.sourceUrl());
        assertEquals(expectedSha256(csv), manifest.sha256());
        assertEquals(manifest, persisted);
    }

    private String expectedSha256(Path path) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = Files.readAllBytes(path);
        byte[] hash = digest.digest(bytes);
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
