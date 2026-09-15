package com.grab.store.catalog.internal.command.handler;

import com.catalog.domain.valueobject.ProductMediaKey;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.storage.FileStoragePort;
import com.grab.framework.storage.PresignedUpload;
import com.grab.framework.storage.StorageAccess;
import com.grab.framework.storage.UploadRequest;
import com.grab.store.catalog.internal.command.CreateStagedMediaUploadCommand;
import com.grab.store.catalog.internal.service.MediaUploadValidator;
import com.grab.store.catalog.internal.command.ProductMediaUploadResult;
import org.springframework.stereotype.Component;

@Component
public class CreateStagedMediaUploadCommandHandler
        implements CommandHandler<CreateStagedMediaUploadCommand, ProductMediaUploadResult> {

    private final FileStoragePort fileStoragePort;
    private final IdGenerator idGenerator;
    private final MediaUploadValidator mediaUploadValidator;

    public CreateStagedMediaUploadCommandHandler(
            FileStoragePort fileStoragePort,
            IdGenerator idGenerator,
            MediaUploadValidator mediaUploadValidator
    ) {
        this.fileStoragePort = fileStoragePort;
        this.idGenerator = idGenerator;
        this.mediaUploadValidator = mediaUploadValidator;
    }

    @Override
    public ProductMediaUploadResult handle(CreateStagedMediaUploadCommand command) {
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

    @Override
    public Class<CreateStagedMediaUploadCommand> getCommandType() {
        return CreateStagedMediaUploadCommand.class;
    }
}
