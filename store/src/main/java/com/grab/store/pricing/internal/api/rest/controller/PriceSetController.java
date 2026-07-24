package com.grab.store.pricing.internal.api.rest.controller;

import com.grab.store.pricing.internal.api.rest.assembler.CalculatedPriceSetModelAssembler;
import com.grab.store.pricing.internal.api.rest.assembler.PriceSetModelAssembler;
import com.grab.store.pricing.internal.api.rest.dto.request.AddPriceRequest;
import com.grab.store.pricing.internal.api.rest.dto.request.CalculatePricesRequest;
import com.grab.store.pricing.internal.api.rest.dto.request.UpdatePriceRequest;
import com.grab.store.pricing.internal.api.rest.dto.response.CalculatedPriceSetResponse;
import com.grab.store.pricing.internal.api.rest.dto.response.PriceSetResponse;
import com.grab.store.pricing.internal.api.rest.service.CalculatePricesQueryService;
import com.grab.store.pricing.internal.api.rest.service.PriceSetCommandService;
import com.grab.store.pricing.internal.api.rest.service.PriceSetQueryService;
import com.grab.store.pricing.internal.config.PricingEnabled;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@PricingEnabled
@RequiredArgsConstructor
@RequestMapping("/api/v1/pricing/price-sets")
public class PriceSetController {

    private final PriceSetCommandService priceSetCommandService;
    private final PriceSetQueryService priceSetQueryService;
    private final CalculatePricesQueryService calculatePricesQueryService;
    private final PriceSetModelAssembler priceSetModelAssembler;
    private final CalculatedPriceSetModelAssembler calculatedPriceSetModelAssembler;

    @PostMapping
    public ResponseEntity<Void> createPriceSet() {
        String priceSetId = priceSetCommandService.createPriceSet();
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(priceSetId)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{priceSetId}")
    public ResponseEntity<EntityModel<PriceSetResponse>> getPriceSet(@PathVariable String priceSetId) {
        PriceSetResponse response = priceSetQueryService.getPriceSet(priceSetId);
        return ResponseEntity.ok(priceSetModelAssembler.toModel(response));
    }

    @PostMapping("/{priceSetId}/prices")
    public ResponseEntity<EntityModel<PriceSetResponse>> addPrice(
            @PathVariable String priceSetId,
            @Valid @RequestBody AddPriceRequest request
    ) {
        PriceSetResponse response = priceSetCommandService.addPrice(priceSetId, request);
        return ResponseEntity.ok(priceSetModelAssembler.toModel(response));
    }

    @PutMapping("/{priceSetId}/prices/{priceId}")
    public ResponseEntity<EntityModel<PriceSetResponse>> updatePrice(
            @PathVariable String priceSetId,
            @PathVariable String priceId,
            @Valid @RequestBody UpdatePriceRequest request
    ) {
        PriceSetResponse response = priceSetCommandService.updatePrice(priceSetId, priceId, request);
        return ResponseEntity.ok(priceSetModelAssembler.toModel(response));
    }

    @DeleteMapping("/{priceSetId}/prices/{priceId}")
    public ResponseEntity<EntityModel<PriceSetResponse>> removePrice(
            @PathVariable String priceSetId,
            @PathVariable String priceId
    ) {
        PriceSetResponse response = priceSetCommandService.removePrice(priceSetId, priceId);
        return ResponseEntity.ok(priceSetModelAssembler.toModel(response));
    }

    @DeleteMapping("/{priceSetId}")
    public ResponseEntity<Void> deletePriceSet(@PathVariable String priceSetId) {
        priceSetCommandService.deletePriceSet(priceSetId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/calculate")
    public ResponseEntity<CollectionModel<EntityModel<CalculatedPriceSetResponse>>> calculatePrices(
            @Valid @RequestBody CalculatePricesRequest request
    ) {
        List<CalculatedPriceSetResponse> responses = calculatePricesQueryService.calculate(request);
        List<EntityModel<CalculatedPriceSetResponse>> models = responses.stream()
                .map(calculatedPriceSetModelAssembler::toModel)
                .toList();
        CollectionModel<EntityModel<CalculatedPriceSetResponse>> collection = CollectionModel.of(models);
        collection.add(linkToCalculate());
        return ResponseEntity.ok(collection);
    }

    private org.springframework.hateoas.Link linkToCalculate() {
        return org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
                .linkTo(org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
                        .methodOn(PriceSetController.class).calculatePrices(null))
                .withRel("calculate-prices");
    }
}
