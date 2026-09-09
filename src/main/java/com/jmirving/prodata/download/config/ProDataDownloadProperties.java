package com.jmirving.prodata.download.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "prodata.download")
public class ProDataDownloadProperties {
    private String googleDriveFolderUrl =
            "https://drive.google.com/drive/folders/1gLSw0RLjBbtaNy0dgnGQDAZOHIgCe-HH";
    private String googleDriveDownloadUrl = "https://drive.google.com/uc";
    private String outputDir = "build/prodata";
    private String tempDir;
    private List<Integer> years = new ArrayList<>();
    private boolean includeAllYears = false;
    private boolean manifestEnabled = false;
    private StructuredOutput structuredOutput = StructuredOutput.NONE;
    private String userAgent = "lol-pro-data-download-cron";
    private Duration connectTimeout = Duration.ofSeconds(30);
    private Duration readTimeout = Duration.ofSeconds(120);

    public String getGoogleDriveFolderUrl() {
        return googleDriveFolderUrl;
    }

    public void setGoogleDriveFolderUrl(String googleDriveFolderUrl) {
        this.googleDriveFolderUrl = googleDriveFolderUrl;
    }

    public String getGoogleDriveDownloadUrl() {
        return googleDriveDownloadUrl;
    }

    public void setGoogleDriveDownloadUrl(String googleDriveDownloadUrl) {
        this.googleDriveDownloadUrl = googleDriveDownloadUrl;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public List<Integer> getYears() {
        return years;
    }

    public void setYears(List<Integer> years) {
        this.years = years;
    }

    public boolean isIncludeAllYears() {
        return includeAllYears;
    }

    public void setIncludeAllYears(boolean includeAllYears) {
        this.includeAllYears = includeAllYears;
    }

    public boolean isManifestEnabled() {
        return manifestEnabled;
    }

    public void setManifestEnabled(boolean manifestEnabled) {
        this.manifestEnabled = manifestEnabled;
    }

    public StructuredOutput getStructuredOutput() {
        return structuredOutput;
    }

    public void setStructuredOutput(StructuredOutput structuredOutput) {
        this.structuredOutput = structuredOutput;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public enum StructuredOutput {
        NONE,
        JSON
    }
}
