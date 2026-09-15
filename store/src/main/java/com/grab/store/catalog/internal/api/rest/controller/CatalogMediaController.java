package com.grab.store.catalog.internal.api.rest.controller;

import com.grab.store.catalog.internal.api.rest.dto.request.CreateProductMediaUploadRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.ProductMediaUploadResponse;
import com.grab.store.catalog.internal.api.rest.service.ProductCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/catalog/media")
@RequiredArgsConstructor
public class CatalogMediaController {

    private final ProductCommandService productCommandService;

    @PostMapping("/uploads")
    public ResponseEntity<EntityModel<ProductMediaUploadResponse>> createStagedMediaUpload(
            @Valid @RequestBody CreateProductMediaUploadRequest request) {
        ProductMediaUploadResponse response = productCommandService.createStagedMediaUpload(request);
        return ResponseEntity.ok(EntityModel.of(response,
                linkTo(methodOn(CatalogMediaController.class).createStagedMediaUpload(null)).withSelfRel()));
    }
}
