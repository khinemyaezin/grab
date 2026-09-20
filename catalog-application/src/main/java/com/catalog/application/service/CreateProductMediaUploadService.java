package com.catalog.application.service;

import lombok.RequiredArgsConstructor;

import com.catalog.application.port.inbound.CreateProductMediaUploadUseCase;

import com.catalog.domain.port.outbound.ProductRepository;
import com.catalog.domain.valueobject.ProductMediaKey;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.storage.FileStoragePort;
import com.grab.framework.storage.PresignedUpload;
import com.grab.framework.storage.StorageAccess;
import com.grab.framework.storage.UploadRequest;
import com.catalog.application.model.write.CreateProductMediaUploadCommand;
import com.catalog.application.model.write.ProductMediaUploadResult;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;

@RequiredArgsConstructor
public class CreateProductMediaUploadService implements CreateProductMediaUploadUseCase {

    private final ProductRepository productRepository;
    private final FileStoragePort fileStoragePort;
    private final IdGenerator idGenerator;
    private final MediaUploadValidator mediaUploadValidator;

    public ProductMediaUploadResult execute(CreateProductMediaUploadCommand command) {
        loadProduct(command);
        mediaUploadValidator.validate(command.filename(), command.contentType(), command.sizeBytes());

        ProductMediaKey storageKey = new ProductMediaKey.Factory(command.merchantId())
                .forProduct(
                        command.productId(),
                        idGenerator.generateId(),
                        mediaUploadValidator.extension(command.filename())
                );
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

    private void loadProduct(CreateProductMediaUploadCommand command) {
        productRepository.find(command.productId(), command.merchantId()).orElseThrow(() ->
                new CatalogServiceException(new CatalogServiceError.ProductNotFound(command.productId().getValue()))
        );
    }
}
