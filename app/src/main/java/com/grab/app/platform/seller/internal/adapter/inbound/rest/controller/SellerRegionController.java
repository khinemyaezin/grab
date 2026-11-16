package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.RegionModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.RegionCountryResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.RegionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerRegionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
public class SellerRegionController {

    private final SellerRegionQueryService queryService;
    private final RegionModelAssembler assembler;

    @GetMapping("/{regionId}")
    public ResponseEntity<EntityModel<RegionResponse>> get(@PathVariable String regionId) {
        return queryService.get(regionId)
                .map(assembler::toModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/countries")
    public ResponseEntity<CollectionModel<EntityModel<RegionCountryResponse>>> countries() {
        List<RegionCountryResponse> responses = queryService.countries();
        return ResponseEntity.ok(assembler.countriesModel(responses));
    }
}
