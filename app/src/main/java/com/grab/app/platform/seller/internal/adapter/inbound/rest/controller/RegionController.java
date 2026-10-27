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

import com.grab.app.platform.seller.internal.adapter.inbound.rest.region.rest.assembler.RegionModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.region.rest.dto.response.RegionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.region.rest.dto.response.RegionCountryResponse;
import com.grab.app.platform.seller.internal.query.RegionCountryQueryService;
import com.grab.app.platform.seller.internal.query.RegionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
public class RegionController implements SellerRestApi {

    private final RegionQueryService regionQueryService;
    private final RegionModelAssembler regionModelAssembler;
    private final RegionCountryQueryService regionCountryQueryService;

    @GetMapping("/{regionId}")
    public ResponseEntity<EntityModel<RegionResponse>> get(@PathVariable String regionId) {
        RegionResponse response = regionQueryService.get(regionId);
        return ResponseEntity.ok(regionModelAssembler.toModel(response));
    }

    @GetMapping("/countries")
    public ResponseEntity<CollectionModel<EntityModel<RegionCountryResponse>>> countries() {
        List<RegionCountryResponse> responses = regionCountryQueryService.list();
        CollectionModel<EntityModel<RegionCountryResponse>> model = CollectionModel.of(
                responses.stream().map(EntityModel::of).toList());
        return ResponseEntity.ok(model);
    }
}
