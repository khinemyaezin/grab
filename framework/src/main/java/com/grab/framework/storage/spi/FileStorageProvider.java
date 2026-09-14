package com.grab.framework.storage.spi;

import com.grab.framework.storage.FileStoragePort;

public interface FileStorageProvider {

    String id();

    int priority();

    boolean isAvailable();

    FileStoragePort createPort(FileStorageConfig config);
}
