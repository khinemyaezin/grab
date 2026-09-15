package com.grab.store.catalog.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record CreateStagedMediaUploadCommand(
        Id merchantId,
        String filename,
        String contentType,
        Long sizeBytes
) implements Command<ProductMediaUploadResult> {
}
