package com.grab.store.catalog.internal.service;

import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class MediaUploadValidator {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "video/mp4"
    );

    private final long maxSizeBytes;

    public MediaUploadValidator(@Value("${storage.upload.max-size-bytes:10485760}") long maxSizeBytes) {
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
