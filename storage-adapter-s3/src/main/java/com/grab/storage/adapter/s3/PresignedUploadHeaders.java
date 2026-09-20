package com.grab.storage.adapter.s3;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class PresignedUploadHeaders {

    private PresignedUploadHeaders() {
    }

    static Map<String, String> fromSigned(Map<String, List<String>> signedHeaders, String contentType) {
        Map<String, String> headers = new LinkedHashMap<>();
        if (signedHeaders != null) {
            signedHeaders.forEach((name, values) -> {
                if (name == null || values == null || values.isEmpty()) {
                    return;
                }
                String canonical = canonicalize(name);
                if ("Host".equals(canonical)) {
                    return;
                }
                headers.putIfAbsent(canonical, values.getFirst());
            });
        }
        if (contentType != null && !contentType.isBlank()) {
            headers.putIfAbsent("Content-Type", contentType);
        }
        return headers;
    }

    static String canonicalize(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return switch (lower) {
            case "content-type" -> "Content-Type";
            case "content-length" -> "Content-Length";
            case "host" -> "Host";
            default -> name;
        };
    }
}
