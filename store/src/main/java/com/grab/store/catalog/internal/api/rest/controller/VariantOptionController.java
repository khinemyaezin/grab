package com.grab.store.catalog.internal.api.rest.controller;

import com.grab.store.catalog.internal.api.rest.dto.response.VariantOptionResponse;
import com.grab.store.catalog.internal.api.rest.service.VariantOptionFacadeService;
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

    private final VariantOptionFacadeService variantOptionFacadeService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<VariantOptionResponse>> getVariantOptionsByName(
            @RequestParam("name") String name,
            @RequestParam("type") String typeId) {
        EntityModel<VariantOptionResponse> response = variantOptionFacadeService.getVariantOptionsByName(name, typeId);
        return ResponseEntity.ok(response);
    }
}
