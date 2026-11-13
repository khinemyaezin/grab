package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.RegionModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.RegionCountryResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.RegionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerRegionQueryService;
import com.grab.app.platform.seller.port.rest.SellerRegionResource;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/regions")
public class SellerRegionController implements SellerRegionResource {

    private final SellerRegionQueryService queryService;
    private final RegionModelAssembler assembler;

    public SellerRegionController(
            SellerRegionQueryService queryService,
            RegionModelAssembler assembler
    ) {
        this.queryService = queryService;
        this.assembler = assembler;
    }

    @Override
    @GetMapping("/{regionId}")
    public ResponseEntity<EntityModel<RegionResponse>> get(@PathVariable String regionId) {
        return queryService.get(regionId)
                .map(assembler::toModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    @GetMapping("/countries")
    public ResponseEntity<CollectionModel<EntityModel<RegionCountryResponse>>> countries() {
        List<RegionCountryResponse> responses = queryService.countries();
        return ResponseEntity.ok(assembler.countriesModel(responses));
    }
}

