package com.catalog.application.service;

import lombok.RequiredArgsConstructor;

import com.catalog.application.port.inbound.CreateStagedMediaUploadUseCase;

import com.catalog.domain.valueobject.ProductMediaKey;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.storage.FileStoragePort;
import com.grab.framework.storage.PresignedUpload;
import com.grab.framework.storage.StorageAccess;
import com.grab.framework.storage.UploadRequest;
import com.catalog.application.command.CreateStagedMediaUploadCommand;
import com.catalog.application.service.MediaUploadValidator;
import com.catalog.application.command.ProductMediaUploadResult;

@RequiredArgsConstructor
public class CreateStagedMediaUploadService implements CreateStagedMediaUploadUseCase {

    private final FileStoragePort fileStoragePort;
    private final IdGenerator idGenerator;
    private final MediaUploadValidator mediaUploadValidator;

    public ProductMediaUploadResult execute(CreateStagedMediaUploadCommand command) {
        mediaUploadValidator.validate(command.filename(), command.contentType(), command.sizeBytes());

        ProductMediaKey storageKey = new ProductMediaKey.Factory(command.merchantId())
                .staged(idGenerator.generateId(), mediaUploadValidator.extension(command.filename()));
        PresignedUpload upload = fileStoragePort.createPresignedUpload(new UploadRequest(
                storageKey.value(),
                command.contentType(),
                command.sizeBytes(),
                StorageAccess.PUBLIC
        ));
        return new ProductMediaUploadResult(
                upload.url(),
                upload.method(),
                upload.requiredHeaders(),
                upload.storageKey(),
                upload.expiresAt()
        );
    }
}
