package com.jmirving.prodata.download.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DownloadJobResult(
        String status,
        String reasonCode,
        String reason,
        String error,
        DownloadJobMetadata metadata
) {
    public static DownloadJobResult success(DownloadJobMetadata metadata) {
        return new DownloadJobResult("SUCCESS", null, null, null, metadata);
    }

    public static DownloadJobResult failure(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = exception.getClass().getSimpleName();
        }
        return new DownloadJobResult("FAILED", "DOWNLOAD_FAILED", "Pro data download failed", message, null);
    }

    @JsonIgnore
    public int exitCode() {
        return "SUCCESS".equals(status) || "SKIPPED".equals(status) ? 0 : 1;
    }
}
