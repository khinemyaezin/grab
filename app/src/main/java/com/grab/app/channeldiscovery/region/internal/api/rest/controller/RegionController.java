package com.grab.app.channeldiscovery.region.internal.api.rest.controller;

import com.grab.app.channeldiscovery.region.internal.api.rest.assembler.RegionModelAssembler;
import com.grab.app.channeldiscovery.region.internal.api.rest.dto.response.RegionResponse;
import com.grab.app.channeldiscovery.region.internal.api.rest.dto.response.RegionCountryResponse;
import com.grab.app.channeldiscovery.region.internal.api.rest.service.RegionCountryQueryService;
import com.grab.app.channeldiscovery.region.internal.api.rest.service.RegionQueryService;
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
public class RegionController {

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
