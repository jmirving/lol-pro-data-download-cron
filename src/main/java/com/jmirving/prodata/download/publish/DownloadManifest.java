package com.jmirving.prodata.download.publish;

import java.time.Instant;

public record DownloadManifest(Instant generatedAt, long rowCount, String sha256, String sourceUrl) {
}
