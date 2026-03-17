package com.grab.store.catalog.internal.api.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

public record ReplaceProductMediaRequest(
        @Valid @NotNull List<Media> medias
) implements Serializable {
    public record Media(
            String id,
            String type,
            @NotBlank String path
    ) implements Serializable {}
}
