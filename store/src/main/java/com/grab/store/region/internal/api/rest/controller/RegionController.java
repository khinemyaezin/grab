package com.grab.store.region.internal.api.rest.controller;

import com.grab.store.region.internal.api.rest.assembler.RegionModelAssembler;
import com.grab.store.region.internal.api.rest.dto.response.RegionResponse;
import com.grab.store.region.internal.api.rest.service.RegionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionQueryService regionQueryService;
    private final RegionModelAssembler regionModelAssembler;

    @GetMapping("/{regionId}")
    public ResponseEntity<EntityModel<RegionResponse>> get(@PathVariable String regionId) {
        RegionResponse response = regionQueryService.get(regionId);
        return ResponseEntity.ok(regionModelAssembler.toModel(response));
    }
}
