package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.PriceSetModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CalculatePriceRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CalculatedPriceResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PriceSetResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerPricingCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerPricingQueryService;
import com.grab.app.platform.seller.port.rest.SellerPriceListResource;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pricing/price-sets")
public class SellerPriceSetController implements SellerPriceListResource.PriceSets {

    private final SellerPricingCommandService commandService;
    private final SellerPricingQueryService queryService;
    private final PriceSetModelAssembler modelAssembler;

    public SellerPriceSetController(
            SellerPricingCommandService commandService,
            SellerPricingQueryService queryService,
            PriceSetModelAssembler modelAssembler) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.modelAssembler = modelAssembler;
    }

    @PostMapping
    public ResponseEntity<EntityModel<PriceSetResponse>> createPriceSet() {
        PriceSetResponse response = commandService.createPriceSet();
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(modelAssembler.toModel(response));
    }

    @GetMapping("/{priceSetId}")
    public ResponseEntity<EntityModel<PriceSetResponse>> getPriceSet(@PathVariable String priceSetId) {
        return queryService.getPriceSet(priceSetId)
                .map(modelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/calculate")
    public ResponseEntity<List<CalculatedPriceResponse>> calculatePrices(@Valid @RequestBody CalculatePriceRequest request) {
        List<CalculatedPriceResponse> results = queryService.calculatePrices(request);
        return ResponseEntity.ok(results);
    }
}
