package com.jmirving.prodata.download.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "prodata.download")
public class ProDataDownloadProperties {
    private String googleDriveFolderUrl =
            "https://drive.google.com/drive/folders/1gLSw0RLjBbtaNy0dgnGQDAZOHIgCe-HH";
    private String outputDir = "build/prodata";
    private String tempDir = "build/prodata/tmp";
    private List<Integer> years = new ArrayList<>();
    private boolean manifestEnabled = false;
    private String userAgent = "lol-pro-data-download-cron";
    private Duration connectTimeout = Duration.ofSeconds(30);
    private Duration readTimeout = Duration.ofSeconds(120);

    public String getGoogleDriveFolderUrl() {
        return googleDriveFolderUrl;
    }

    public void setGoogleDriveFolderUrl(String googleDriveFolderUrl) {
        this.googleDriveFolderUrl = googleDriveFolderUrl;
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

    public boolean isManifestEnabled() {
        return manifestEnabled;
    }

    public void setManifestEnabled(boolean manifestEnabled) {
        this.manifestEnabled = manifestEnabled;
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
}
