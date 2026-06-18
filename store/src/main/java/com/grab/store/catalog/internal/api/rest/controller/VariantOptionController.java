package com.grab.store.catalog.internal.api.rest.controller;

import com.grab.store.catalog.internal.api.rest.assembler.VariantOptionModelAssembler;
import com.grab.store.catalog.internal.api.rest.dto.response.VariantOptionResponse;
import com.grab.store.catalog.internal.api.rest.service.VariantOptionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog/variant-options")
@RequiredArgsConstructor
public class VariantOptionController {

    private final VariantOptionQueryService variantOptionQueryService;
    private final VariantOptionModelAssembler variantOptionModelAssembler;

    @GetMapping()
    public ResponseEntity<EntityModel<VariantOptionResponse>> getVariantOptionsByName(
            @RequestParam("name") String name,
            @RequestParam("typeId") String typeId) {
        VariantOptionResponse response = variantOptionQueryService.getVariantOptionsByName(name, typeId);
        return ResponseEntity.ok(variantOptionModelAssembler.toModel(response, name, typeId));
    }
}
