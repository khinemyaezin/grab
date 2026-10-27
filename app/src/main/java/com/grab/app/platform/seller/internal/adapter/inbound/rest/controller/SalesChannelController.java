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

import com.grab.app.platform.seller.internal.adapter.inbound.rest.saleschannel.rest.assembler.SalesChannelModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.saleschannel.rest.dto.response.SalesChannelResponse;
import com.grab.app.platform.seller.internal.query.SalesChannelQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sales-channels/channels")
@RequiredArgsConstructor
public class SalesChannelController implements SellerRestApi {

    private final SalesChannelQueryService salesChannelQueryService;
    private final SalesChannelModelAssembler salesChannelModelAssembler;

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<SalesChannelResponse>>> list(
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<SalesChannelResponse> pagedResourcesAssembler
    ) {
        Page<SalesChannelResponse> page = salesChannelQueryService.list(pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(page, salesChannelModelAssembler));
    }

    @GetMapping("/{salesChannelId}")
    public ResponseEntity<EntityModel<SalesChannelResponse>> get(@PathVariable String salesChannelId) {
        SalesChannelResponse response = salesChannelQueryService.get(salesChannelId);
        return ResponseEntity.ok(salesChannelModelAssembler.toModel(response));
    }
}
