package com.jmirving.prodata.download.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class DownloadJobResultTest {
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void serializesAsGenericStructuredEnvelopeWithoutProcessOnlyFields() throws Exception {
        DownloadJobResult result = DownloadJobResult.success(
                new DownloadJobMetadata("/work/output", List.of(2026), List.of())
        );

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(result));

        assertEquals("SUCCESS", json.get("status").asText());
        assertEquals("/work/output", json.get("metadata").get("outputDirectory").asText());
        assertTrue(json.get("metadata").get("artifacts").isArray());
        assertFalse(json.has("exitCode"));
        assertFalse(json.has("reasonCode"));
        assertFalse(json.has("reason"));
        assertFalse(json.has("error"));
    }

    @Test
    void serializesFailureAndMapsItToNonZeroExit() throws Exception {
        DownloadJobResult result = DownloadJobResult.failure(new IllegalStateException("boom"));

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(result));

        assertEquals("FAILED", json.get("status").asText());
        assertEquals("DOWNLOAD_FAILED", json.get("reasonCode").asText());
        assertEquals("boom", json.get("error").asText());
        assertEquals(1, result.exitCode());
    }
}
