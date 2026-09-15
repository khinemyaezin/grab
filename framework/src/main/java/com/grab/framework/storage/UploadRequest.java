package com.grab.framework.storage;

public record UploadRequest(
        String storageKey,
        String contentType,
        Long sizeBytes,
        StorageAccess access
) {
    public UploadRequest {
        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException("storageKey is required");
        }
        if (access == null) {
            access = StorageAccess.PUBLIC;
        }
    }
}
