package com.grab.storage.adapter.s3;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PresignedUploadHeadersTest {

    @Test
    void collapsesDuplicateContentTypeCasingIntoOneHeader() {
        Map<String, String> headers = PresignedUploadHeaders.fromSigned(
                Map.of("content-type", List.of("image/jpeg")),
                "image/jpeg"
        );

        assertEquals(1, headers.size());
        assertEquals("image/jpeg", headers.get("Content-Type"));
        assertFalse(headers.containsKey("content-type"));
    }

    @Test
    void dropsHostBecauseTheBrowserSetsItFromTheUrl() {
        Map<String, String> headers = PresignedUploadHeaders.fromSigned(
                Map.of(
                        "host", List.of("seaweedfs:8333"),
                        "content-type", List.of("image/jpeg"),
                        "content-length", List.of("12")
                ),
                "image/png"
        );

        assertEquals("image/jpeg", headers.get("Content-Type"));
        assertEquals("12", headers.get("Content-Length"));
        assertFalse(headers.containsKey("Host"));
        assertFalse(headers.containsKey("host"));
    }

    @Test
    void fillsContentTypeWhenSignedHeadersOmitIt() {
        Map<String, String> headers = PresignedUploadHeaders.fromSigned(Map.of(), "image/webp");

        assertEquals("image/webp", headers.get("Content-Type"));
    }
}
