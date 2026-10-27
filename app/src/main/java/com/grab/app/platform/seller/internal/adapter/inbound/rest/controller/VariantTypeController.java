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

import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.assembler.VariantTypeModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.VariantTypeResponse;
import com.grab.app.platform.seller.internal.query.VariantTypeQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog/variant-types")
@RequiredArgsConstructor
public class VariantTypeController implements SellerRestApi {

    private final VariantTypeQueryService variantTypeQueryService;
    private final VariantTypeModelAssembler variantTypeModelAssembler;

    @GetMapping()
    public ResponseEntity<EntityModel<VariantTypeResponse>> getVariantTypesByName(
            @RequestParam("name") String name) {
        VariantTypeResponse response = variantTypeQueryService.getVariantTypesByName(name);
        return ResponseEntity.ok(variantTypeModelAssembler.toModel(response, name));
    }
}
