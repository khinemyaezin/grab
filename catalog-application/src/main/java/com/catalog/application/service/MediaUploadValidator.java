package com.catalog.application.service;

import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import java.util.Locale;
import java.util.Set;

public class MediaUploadValidator {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "video/mp4"
    );

    private final long maxSizeBytes;

    public MediaUploadValidator(long maxSizeBytes) {
        this.maxSizeBytes = maxSizeBytes;
    }

    public void validate(String filename, String contentType, Long sizeBytes) {
        if (filename == null || filename.isBlank()) {
            throw new CatalogServiceException(new CatalogServiceError.InvalidMediaUpload("filename is required"));
        }
        String normalizedContentType = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        if (!ALLOWED_CONTENT_TYPES.contains(normalizedContentType)) {
            throw new CatalogServiceException(new CatalogServiceError.InvalidMediaUpload("contentType is not allowed"));
        }
        if (sizeBytes != null && sizeBytes > maxSizeBytes) {
            throw new CatalogServiceException(new CatalogServiceError.InvalidMediaUpload("file exceeds max size"));
        }
    }

    public String extension(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9.]", "");
    }
}
