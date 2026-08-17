package com.grab.store.pricing.internal.api.rest.controller;

import com.grab.store.pricing.internal.api.rest.assembler.VariantPriceSetLinkModelAssembler;
import com.grab.store.pricing.internal.api.rest.dto.request.ListVariantPriceSetLinksRequest;
import com.grab.store.pricing.internal.api.rest.dto.response.VariantPriceSetLinkResponse;
import com.grab.store.pricing.internal.api.rest.service.VariantPriceSetLinkQueryService;
import com.grab.store.pricing.internal.config.PricingEnabled;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@PricingEnabled
@RequiredArgsConstructor
@RequestMapping("/api/v1/pricing/variant-price-links")
public class VariantPriceSetLinkController {

    private final VariantPriceSetLinkQueryService queryService;
    private final VariantPriceSetLinkModelAssembler modelAssembler;

    @PostMapping
    public ResponseEntity<CollectionModel<EntityModel<VariantPriceSetLinkResponse>>> list(
            @Valid @RequestBody ListVariantPriceSetLinksRequest request
    ) {
        List<EntityModel<VariantPriceSetLinkResponse>> models = queryService.findByVariantIds(request).stream()
                .map(modelAssembler::toModel)
                .toList();
        CollectionModel<EntityModel<VariantPriceSetLinkResponse>> collection = CollectionModel.of(models);
        return ResponseEntity.ok(collection);
    }
}
