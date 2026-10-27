package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.port.rest.SellerRestApi;

import com.grab.app.platform.seller.internal.command.*;
import com.grab.app.platform.seller.internal.query.*;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.request.CreateProductMediaUploadRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.ProductMediaUploadResponse;
import com.grab.app.platform.seller.internal.command.ProductCommandService;
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
public class CatalogMediaController implements SellerRestApi {

    private final ProductCommandService productCommandService;

    @PostMapping("/uploads")
    public ResponseEntity<EntityModel<ProductMediaUploadResponse>> createStagedMediaUpload(
            @Valid @RequestBody CreateProductMediaUploadRequest request) {
        ProductMediaUploadResponse response = productCommandService.createStagedMediaUpload(request);
        return ResponseEntity.ok(EntityModel.of(response,
                linkTo(methodOn(CatalogMediaController.class).createStagedMediaUpload(null)).withSelfRel()));
    }
}
