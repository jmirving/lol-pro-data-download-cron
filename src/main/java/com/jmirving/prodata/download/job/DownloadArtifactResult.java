package com.jmirving.prodata.download.job;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DownloadArtifactResult(
        int year,
        String fileName,
        String path,
        String manifestPath,
        Instant generatedAt,
        long rowCount,
        String sha256,
        String sourceUrl
) {
}
