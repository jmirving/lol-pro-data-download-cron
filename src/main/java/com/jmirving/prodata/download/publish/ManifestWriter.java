package com.jmirving.prodata.download.publish;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ManifestWriter {
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public ManifestWriter() {
        this(new ObjectMapper().findAndRegisterModules(), Clock.systemUTC());
    }

    public ManifestWriter(ObjectMapper objectMapper, Clock clock) {
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public DownloadManifest write(Path csvPath, Path manifestPath, String sourceUrl) throws IOException {
        DownloadManifest manifest = buildManifest(csvPath, sourceUrl);
        objectMapper.writeValue(manifestPath.toFile(), manifest);
        return manifest;
    }

    public DownloadManifest buildManifest(Path csvPath, String sourceUrl) throws IOException {
        return new DownloadManifest(
                Instant.now(clock),
                countRows(csvPath),
                sha256(csvPath),
                sourceUrl
        );
    }

    private long countRows(Path csvPath) throws IOException {
        long count = 0;
        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            String line = reader.readLine();
            if (line == null) {
                return 0;
            }
            while (reader.readLine() != null) {
                count++;
            }
        }
        return count;
    }

    private String sha256(Path csvPath) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }

        byte[] buffer = new byte[8192];
        int read;
        try (var inputStream = Files.newInputStream(csvPath)) {
            while ((read = inputStream.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
        }

        StringBuilder hex = new StringBuilder();
        for (byte b : digest.digest()) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
