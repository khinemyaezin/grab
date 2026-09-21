package com.catalog.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

import java.util.List;

public record ReplaceProductMediaCommand(
        Id merchantId,
        Id productId,
        List<Media> medias
) implements Command<ProductMediaResult> {
    public record Media(
            Id id,
            String storageKey,
            String contentType,
            Integer rank
    ) {}
}
