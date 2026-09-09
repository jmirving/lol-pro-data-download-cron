package com.jmirving.prodata.download.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jmirving.prodata.download.config.ProDataDownloadProperties;
import com.jmirving.prodata.download.provider.DownloadProvider;
import com.jmirving.prodata.download.provider.RemoteFile;
import com.jmirving.prodata.download.publish.AtomicFilePublisher;
import com.jmirving.prodata.download.publish.ManifestWriter;
import com.jmirving.prodata.download.select.YearFileSelector;
import com.jmirving.prodata.download.validate.CsvHeaderValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProDataDownloadJobTest {
    @TempDir
    Path tempDir;

    @Test
    void downloadsValidatesAndPublishesMultipleYears() {
        RemoteFile file2025 = new RemoteFile("id2025", "2025_LoL_esports_match_data_from_OraclesElixir.csv");
        RemoteFile file2026 = new RemoteFile("id2026", "2026_LoL_esports_match_data_from_OraclesElixir.csv");
        Map<String, String> data = new HashMap<>();
        data.put(file2025.id(), sampleCsv(2025));
        data.put(file2026.id(), sampleCsv(2026));

        DownloadProvider provider = new StubDownloadProvider(List.of(file2025, file2026), data);

        ProDataDownloadProperties properties = new ProDataDownloadProperties();
        properties.setOutputDir(tempDir.resolve("out").toString());
        properties.setTempDir(tempDir.resolve("out").toString());
        properties.setYears(List.of(2025, 2026));
        properties.setManifestEnabled(true);

        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        ProDataDownloadJob job = new ProDataDownloadJob(
                properties,
                provider,
                new YearFileSelector(Clock.fixed(Instant.parse("2026-01-15T00:00:00Z"), ZoneOffset.UTC)),
                new CsvHeaderValidator(),
                new AtomicFilePublisher(),
                new ManifestWriter(objectMapper, Clock.fixed(Instant.parse("2026-01-15T00:00:00Z"), ZoneOffset.UTC))
        );

        int exitCode = job.run();

        assertEquals(0, exitCode);
        assertTrue(Files.exists(tempDir.resolve("out").resolve(file2025.name())));
        assertTrue(Files.exists(tempDir.resolve("out").resolve(file2026.name())));
        assertTrue(Files.exists(tempDir.resolve("out").resolve(file2025.name() + ".manifest.json")));
        assertTrue(Files.exists(tempDir.resolve("out").resolve(file2026.name() + ".manifest.json")));
    }

    @Test
    void reportsPublishedArtifactsForStructuredConsumers() {
        RemoteFile file = new RemoteFile("id2026", "2026_LoL_esports_match_data_from_OraclesElixir.csv");
        DownloadProvider provider = new StubDownloadProvider(List.of(file), Map.of(file.id(), sampleCsv(2026)));

        ProDataDownloadProperties properties = new ProDataDownloadProperties();
        properties.setOutputDir(tempDir.resolve("ephemeral-output").toString());
        properties.setYears(List.of(2026));
        properties.setManifestEnabled(true);

        ProDataDownloadJob job = new ProDataDownloadJob(
                properties,
                provider,
                new YearFileSelector(Clock.fixed(Instant.parse("2026-01-15T00:00:00Z"), ZoneOffset.UTC)),
                new CsvHeaderValidator(),
                new AtomicFilePublisher(),
                new ManifestWriter(new ObjectMapper().findAndRegisterModules(),
                        Clock.fixed(Instant.parse("2026-01-15T00:00:00Z"), ZoneOffset.UTC))
        );

        DownloadJobResult result = job.runWithResult();

        assertEquals("SUCCESS", result.status());
        assertEquals(0, result.exitCode());
        assertEquals(tempDir.resolve("ephemeral-output").toAbsolutePath().toString(),
                result.metadata().outputDirectory());
        assertEquals(List.of(2026), result.metadata().years());
        assertEquals(1, result.metadata().artifacts().size());
        DownloadArtifactResult artifact = result.metadata().artifacts().get(0);
        assertEquals(2026, artifact.year());
        assertEquals(file.name(), artifact.fileName());
        assertEquals(1, artifact.rowCount());
        assertEquals("stub://id2026", artifact.sourceUrl());
        assertNotNull(artifact.sha256());
        assertNotNull(artifact.manifestPath());
        assertTrue(Files.exists(Path.of(artifact.path())));
        assertTrue(Files.exists(Path.of(artifact.manifestPath())));
        assertFalse(Files.exists(tempDir.resolve("ephemeral-output").resolve("tmp"))
                && hasRegularFiles(tempDir.resolve("ephemeral-output").resolve("tmp")));
    }

    @Test
    void returnsMachineReadableFailureWithNonZeroExitCode() {
        ProDataDownloadProperties properties = new ProDataDownloadProperties();
        properties.setOutputDir(tempDir.resolve("failed-output").toString());
        properties.setYears(List.of(2026));

        ProDataDownloadJob job = new ProDataDownloadJob(
                properties,
                new StubDownloadProvider(List.of(), Map.of()),
                new YearFileSelector(Clock.fixed(Instant.parse("2026-01-15T00:00:00Z"), ZoneOffset.UTC)),
                new CsvHeaderValidator(),
                new AtomicFilePublisher(),
                new ManifestWriter(new ObjectMapper().findAndRegisterModules(), Clock.systemUTC())
        );

        DownloadJobResult result = job.runWithResult();

        assertEquals("FAILED", result.status());
        assertEquals("DOWNLOAD_FAILED", result.reasonCode());
        assertEquals(1, result.exitCode());
        assertTrue(result.error().contains("Missing CSVs for years: [2026]"));
    }

    @Test
    void validationFailurePreservesPublishedFileAndCleansTemporaryDownload() throws IOException {
        RemoteFile file = new RemoteFile("id2026", "2026_LoL_esports_match_data_from_OraclesElixir.csv");
        Path outputDir = tempDir.resolve("repeat-safe-output");
        Files.createDirectories(outputDir);
        Path published = outputDir.resolve(file.name());
        Files.writeString(published, "previous-good-data");

        ProDataDownloadProperties properties = new ProDataDownloadProperties();
        properties.setOutputDir(outputDir.toString());
        properties.setYears(List.of(2026));

        ProDataDownloadJob job = new ProDataDownloadJob(
                properties,
                new StubDownloadProvider(List.of(file), Map.of(file.id(), "not,a,valid,header\n")),
                new YearFileSelector(Clock.fixed(Instant.parse("2026-01-15T00:00:00Z"), ZoneOffset.UTC)),
                new CsvHeaderValidator(),
                new AtomicFilePublisher(),
                new ManifestWriter(new ObjectMapper().findAndRegisterModules(), Clock.systemUTC())
        );

        DownloadJobResult result = job.runWithResult();

        assertEquals("FAILED", result.status());
        assertEquals("previous-good-data", Files.readString(published));
        assertFalse(hasRegularFiles(outputDir.resolve("tmp")));
    }

    @Test
    void downloadsAllAvailableYearsWhenIncludeAllYearsEnabled() {
        RemoteFile file2024 = new RemoteFile("id2024", "2024_LoL_esports_match_data_from_OraclesElixir.csv");
        RemoteFile file2026 = new RemoteFile("id2026", "2026_LoL_esports_match_data_from_OraclesElixir.csv");
        Map<String, String> data = new HashMap<>();
        data.put(file2024.id(), sampleCsv(2024));
        data.put(file2026.id(), sampleCsv(2026));

        DownloadProvider provider = new StubDownloadProvider(List.of(file2024, file2026), data);

        ProDataDownloadProperties properties = new ProDataDownloadProperties();
        properties.setOutputDir(tempDir.resolve("out-all").toString());
        properties.setTempDir(tempDir.resolve("out-all").toString());
        properties.setIncludeAllYears(true);

        ProDataDownloadJob job = new ProDataDownloadJob(
                properties,
                provider,
                new YearFileSelector(Clock.fixed(Instant.parse("2026-01-15T00:00:00Z"), ZoneOffset.UTC)),
                new CsvHeaderValidator(),
                new AtomicFilePublisher(),
                new ManifestWriter(new ObjectMapper().findAndRegisterModules(), Clock.fixed(Instant.parse("2026-01-15T00:00:00Z"), ZoneOffset.UTC))
        );

        int exitCode = job.run();

        assertEquals(0, exitCode);
        assertTrue(Files.exists(tempDir.resolve("out-all").resolve(file2024.name())));
        assertTrue(Files.exists(tempDir.resolve("out-all").resolve(file2026.name())));
    }

    private String sampleCsv(int year) {
        return String.join(
                ",",
                "gameid",
                "league",
                "split",
                "year",
                "date",
                "game",
                "patch",
                "participantid",
                "side",
                "teamid",
                "ban1",
                "ban2",
                "ban3",
                "ban4",
                "ban5",
                "pick1",
                "pick2",
                "pick3",
                "pick4",
                "pick5"
        ) + "\n" +
                String.join(
                        ",",
                        "1",
                        "LCS",
                        "Spring",
                        String.valueOf(year),
                        "2026-01-15",
                        "1",
                        "15.1",
                        "100",
                        "Blue",
                        "1",
                        "A",
                        "B",
                        "C",
                        "D",
                        "E",
                        "F",
                        "G",
                        "H",
                        "I",
                        "J"
                ) + "\n";
    }

    private boolean hasRegularFiles(Path directory) {
        try (var entries = Files.list(directory)) {
            return entries.anyMatch(Files::isRegularFile);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static class StubDownloadProvider implements DownloadProvider {
        private final List<RemoteFile> files;
        private final Map<String, String> dataById;

        private StubDownloadProvider(List<RemoteFile> files, Map<String, String> dataById) {
            this.files = files;
            this.dataById = dataById;
        }

        @Override
        public List<RemoteFile> listFiles() {
            return files;
        }

        @Override
        public void download(RemoteFile file, Path destination) throws IOException {
            Files.writeString(destination, dataById.get(file.id()));
        }

        @Override
        public String sourceUrl(RemoteFile file) {
            return "stub://" + file.id();
        }
    }
}
