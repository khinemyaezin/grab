package com.grab.store.catalog.internal.api.rest.controller;

import com.grab.store.catalog.internal.api.rest.assembler.VariantTypeModelAssembler;
import com.grab.store.catalog.internal.api.rest.dto.response.VariantTypeResponse;
import com.grab.store.catalog.internal.api.rest.service.VariantTypeQueryService;
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
public class VariantTypeController {

    private final VariantTypeQueryService variantTypeQueryService;
    private final VariantTypeModelAssembler variantTypeModelAssembler;

    @GetMapping()
    public ResponseEntity<EntityModel<VariantTypeResponse>> getVariantTypesByName(
            @RequestParam("name") String name) {
        VariantTypeResponse response = variantTypeQueryService.getVariantTypesByName(name);
        return ResponseEntity.ok(variantTypeModelAssembler.toModel(response, name));
    }
}
