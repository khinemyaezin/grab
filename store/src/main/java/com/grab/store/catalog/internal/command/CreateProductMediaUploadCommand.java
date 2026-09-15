package com.grab.store.catalog.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record CreateProductMediaUploadCommand(
        Id merchantId,
        Id productId,
        String filename,
        String contentType,
        Long sizeBytes
) implements Command<ProductMediaUploadResult> {
}
