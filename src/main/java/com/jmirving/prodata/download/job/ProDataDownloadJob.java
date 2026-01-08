package com.jmirving.prodata.download.job;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.jmirving.prodata.download.config.ProDataDownloadProperties;
import com.jmirving.prodata.download.provider.DownloadProvider;
import com.jmirving.prodata.download.provider.RemoteFile;
import com.jmirving.prodata.download.publish.AtomicFilePublisher;
import com.jmirving.prodata.download.publish.DownloadManifest;
import com.jmirving.prodata.download.publish.ManifestWriter;
import com.jmirving.prodata.download.select.YearFileSelector;
import com.jmirving.prodata.download.validate.CsvHeaderValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProDataDownloadJob {
    private static final Logger logger = LoggerFactory.getLogger(ProDataDownloadJob.class);

    private final ProDataDownloadProperties properties;
    private final DownloadProvider downloadProvider;
    private final YearFileSelector yearFileSelector;
    private final CsvHeaderValidator csvHeaderValidator;
    private final AtomicFilePublisher filePublisher;
    private final ManifestWriter manifestWriter;

    public ProDataDownloadJob(
            ProDataDownloadProperties properties,
            DownloadProvider downloadProvider,
            YearFileSelector yearFileSelector,
            CsvHeaderValidator csvHeaderValidator,
            AtomicFilePublisher filePublisher,
            ManifestWriter manifestWriter
    ) {
        this.properties = properties;
        this.downloadProvider = downloadProvider;
        this.yearFileSelector = yearFileSelector;
        this.csvHeaderValidator = csvHeaderValidator;
        this.filePublisher = filePublisher;
        this.manifestWriter = manifestWriter;
    }

    public int run() {
        try {
            execute();
            return 0;
        } catch (Exception e) {
            logger.error("Pro data download failed", e);
            return 1;
        }
    }

    private void execute() throws IOException, InterruptedException {
        Path outputDir = resolveOutputDir();
        Path tempDir = resolveTempDir(outputDir);
        ensureSameFileStore(outputDir, tempDir);

        List<RemoteFile> availableFiles = downloadProvider.listFiles();
        List<Integer> targetYears = yearFileSelector.resolveTargetYears(properties.getYears());
        List<RemoteFile> selectedFiles = yearFileSelector.select(availableFiles, targetYears);
        List<Integer> missingYears = findMissingYears(targetYears, selectedFiles);
        if (!missingYears.isEmpty()) {
            throw new IllegalStateException("Missing CSVs for years: " + missingYears);
        }

        logger.info("Downloading years {} from {}", targetYears, properties.getGoogleDriveFolderUrl());
        for (RemoteFile file : selectedFiles) {
            Path tempFile = createTempFile(tempDir, file.name());
            downloadProvider.download(file, tempFile);
            csvHeaderValidator.validate(tempFile);

            Path destination = outputDir.resolve(file.name());
            filePublisher.publish(tempFile, destination);

            String sourceUrl = downloadProvider.sourceUrl(file);
            DownloadManifest manifest = properties.isManifestEnabled()
                    ? manifestWriter.write(destination, manifestPath(destination), sourceUrl)
                    : manifestWriter.buildManifest(destination, sourceUrl);

            logger.info(
                    "Published {} (rows={}, sha256={}, source={})",
                    destination,
                    manifest.rowCount(),
                    manifest.sha256(),
                    manifest.sourceUrl()
            );
        }
    }

    private Path resolveOutputDir() {
        String outputDir = properties.getOutputDir();
        if (outputDir == null || outputDir.isBlank()) {
            throw new IllegalStateException("prodata.download.outputDir is required");
        }
        Path path = Paths.get(outputDir).toAbsolutePath();
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create output directory: " + path, e);
        }
        return path;
    }

    private Path resolveTempDir(Path outputDir) {
        String configuredTempDir = properties.getTempDir();
        Path tempDir = configuredTempDir == null || configuredTempDir.isBlank()
                ? outputDir
                : Paths.get(configuredTempDir).toAbsolutePath();
        try {
            Files.createDirectories(tempDir);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create temp directory: " + tempDir, e);
        }
        return tempDir;
    }

    private void ensureSameFileStore(Path outputDir, Path tempDir) throws IOException {
        FileStore outputStore = Files.getFileStore(outputDir);
        FileStore tempStore = Files.getFileStore(tempDir);
        if (!outputStore.equals(tempStore)) {
            throw new IllegalStateException("Temp directory must be on the same filesystem for atomic moves");
        }
    }

    private Path createTempFile(Path tempDir, String filename) throws IOException {
        String prefix = filename.toLowerCase(Locale.ROOT).replace(".csv", "");
        if (prefix.length() < 3) {
            prefix = "oe";
        }
        return Files.createTempFile(tempDir, prefix + "-", ".download");
    }

    private List<Integer> findMissingYears(List<Integer> targetYears, List<RemoteFile> selectedFiles) {
        Set<Integer> present = new HashSet<>();
        for (RemoteFile file : selectedFiles) {
            String name = file.name();
            if (name != null && name.length() >= 4) {
                String yearText = name.substring(0, 4);
                if (yearText.chars().allMatch(Character::isDigit)) {
                    present.add(Integer.parseInt(yearText));
                }
            }
        }
        return targetYears.stream().filter(year -> !present.contains(year)).toList();
    }

    private Path manifestPath(Path destination) {
        return destination.resolveSibling(destination.getFileName() + ".manifest.json");
    }
}
