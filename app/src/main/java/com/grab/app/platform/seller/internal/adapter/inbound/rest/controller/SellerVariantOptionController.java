package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.VariantOptionModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.VariantTypeModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.VariantOptionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.VariantTypeResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerVariantDefinitionQueryService;
import com.grab.app.platform.seller.port.rest.SellerProductResource;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/variant-options")
public class SellerVariantOptionController implements SellerProductResource.VariantOptions {

    private final SellerVariantDefinitionQueryService variantDefinitionQueryService;
    private final VariantTypeModelAssembler variantTypeModelAssembler;
    private final VariantOptionModelAssembler variantOptionModelAssembler;

    public SellerVariantOptionController(
            SellerVariantDefinitionQueryService variantDefinitionQueryService,
            VariantTypeModelAssembler variantTypeModelAssembler,
            VariantOptionModelAssembler variantOptionModelAssembler) {
        this.variantDefinitionQueryService = variantDefinitionQueryService;
        this.variantTypeModelAssembler = variantTypeModelAssembler;
        this.variantOptionModelAssembler = variantOptionModelAssembler;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<VariantOptionResponse>>> listVariantOptions(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String typeId) {
        List<VariantOptionResponse> options = variantDefinitionQueryService.listOptions(name, typeId);
        return ResponseEntity.ok(variantOptionModelAssembler.listVariantOptionsModel(options, name, typeId));
    }

    @GetMapping("/types")
    public ResponseEntity<CollectionModel<EntityModel<VariantTypeResponse>>> listVariantTypes(
            @RequestParam(required = false) String name) {
        List<VariantTypeResponse> types = variantDefinitionQueryService.listTypes(name);
        return ResponseEntity.ok(variantTypeModelAssembler.listVariantTypesModel(types, name));
    }
}
