package com.grab.store.catalog.internal.command;

import java.util.List;

public record ProductMediaResult(
        String productId,
        List<GetProductPayload.Media> medias
) {
}
