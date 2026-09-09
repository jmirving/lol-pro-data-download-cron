package com.jmirving.prodata.download.job;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DownloadJobMetadata(
        String outputDirectory,
        List<Integer> years,
        List<DownloadArtifactResult> artifacts
) {
}
