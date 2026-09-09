package com.jmirving.prodata.download.publish;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DownloadManifest(
        @JsonProperty("generated_at") Instant generatedAt,
        @JsonProperty("row_count") long rowCount,
        String sha256,
        @JsonProperty("source_url") String sourceUrl
) {
}
