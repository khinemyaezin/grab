package com.catalog.application.command;

import com.grab.framework.id.Id;

import java.util.List;

public record ProductMediaResult(
        String productId,
        List<Media> medias
) {
    public ProductMediaResult {
        medias = medias == null ? List.of() : List.copyOf(medias);
    }

    public record Media(
            Id id,
            String storageKey,
            String url,
            String contentType,
            int rank
    ) {
    }
}
