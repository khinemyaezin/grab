package com.grab.framework.storage;

/**
 * Stable storage contract for callers. Adapters live outside {@code framework}.
 */
public interface FileStoragePort {

    PresignedUpload createPresignedUpload(UploadRequest request);

    boolean objectExists(String storageKey);

    String resolvePublicUrl(String storageKey);

    PresignedUrl createPresignedGet(String storageKey);

    void delete(String storageKey);
}
