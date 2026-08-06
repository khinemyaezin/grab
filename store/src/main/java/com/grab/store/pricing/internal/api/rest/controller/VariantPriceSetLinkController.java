package com.grab.store.pricing.internal.api.rest.controller;

import com.grab.store.pricing.internal.api.rest.assembler.VariantPriceSetLinkModelAssembler;
import com.grab.store.pricing.internal.api.rest.dto.response.VariantPriceSetLinkResponse;
import com.grab.store.pricing.internal.api.rest.service.VariantPriceSetLinkQueryService;
import com.grab.store.pricing.internal.config.PricingEnabled;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@PricingEnabled
@RequiredArgsConstructor
@RequestMapping("/api/v1/pricing/variant-price-links")
public class VariantPriceSetLinkController {

    private final VariantPriceSetLinkQueryService queryService;
    private final VariantPriceSetLinkModelAssembler modelAssembler;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<VariantPriceSetLinkResponse>>> list(
            @RequestParam List<String> variantIds
    ) {
        List<EntityModel<VariantPriceSetLinkResponse>> models = queryService.findByVariantIds(variantIds).stream()
                .map(modelAssembler::toModel)
                .toList();
        CollectionModel<EntityModel<VariantPriceSetLinkResponse>> collection = CollectionModel.of(models);
        collection.add(linkTo(methodOn(VariantPriceSetLinkController.class).list(null))
                .withRel("list-variant-price-links"));
        return ResponseEntity.ok(collection);
    }
}
