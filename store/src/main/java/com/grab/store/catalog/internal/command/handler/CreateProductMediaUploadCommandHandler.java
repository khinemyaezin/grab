package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.repository.ProductRepository;
import com.catalog.domain.valueobject.ProductMediaKey;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.storage.FileStoragePort;
import com.grab.framework.storage.PresignedUpload;
import com.grab.framework.storage.StorageAccess;
import com.grab.framework.storage.UploadRequest;
import com.grab.store.catalog.internal.command.CreateProductMediaUploadCommand;
import com.grab.store.catalog.internal.service.MediaUploadValidator;
import com.grab.store.catalog.internal.command.ProductMediaUploadResult;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import org.springframework.stereotype.Component;

@Component
public class CreateProductMediaUploadCommandHandler
        implements CommandHandler<CreateProductMediaUploadCommand, ProductMediaUploadResult> {

    private final ProductRepository productRepository;
    private final FileStoragePort fileStoragePort;
    private final IdGenerator idGenerator;
    private final MediaUploadValidator mediaUploadValidator;

    public CreateProductMediaUploadCommandHandler(
            ProductRepository productRepository,
            FileStoragePort fileStoragePort,
            IdGenerator idGenerator,
            MediaUploadValidator mediaUploadValidator
    ) {
        this.productRepository = productRepository;
        this.fileStoragePort = fileStoragePort;
        this.idGenerator = idGenerator;
        this.mediaUploadValidator = mediaUploadValidator;
    }

    @Override
    @CatalogReadTransactional
    public ProductMediaUploadResult handle(CreateProductMediaUploadCommand command) {
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

    @Override
    public Class<CreateProductMediaUploadCommand> getCommandType() {
        return CreateProductMediaUploadCommand.class;
    }

    private void loadProduct(CreateProductMediaUploadCommand command) {
        productRepository.find(command.productId(), command.merchantId()).orElseThrow(() ->
                new CatalogServiceException(new CatalogServiceError.ProductNotFound(command.productId().getValue()))
        );
    }
}
