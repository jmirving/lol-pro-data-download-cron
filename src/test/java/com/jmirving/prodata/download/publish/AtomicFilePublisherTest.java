package com.jmirving.prodata.download.publish;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AtomicFilePublisherTest {
    @TempDir
    Path tempDir;

    @Test
    void movesFileIntoPlace() throws IOException {
        Path outputDir = tempDir.resolve("out");
        Files.createDirectories(outputDir);

        Path tempFile = tempDir.resolve("temp.csv");
        Files.writeString(tempFile, "data");

        Path destination = outputDir.resolve("2026_LoL_esports_match_data_from_OraclesElixir.csv");

        AtomicFilePublisher publisher = new AtomicFilePublisher();
        publisher.publish(tempFile, destination);

        assertTrue(Files.exists(destination));
        assertFalse(Files.exists(tempFile));
        assertEquals("data", Files.readString(destination));
    }
}
