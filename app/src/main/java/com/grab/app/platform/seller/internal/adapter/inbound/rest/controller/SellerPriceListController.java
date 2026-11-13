package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.PriceListModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreatePriceListRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PriceListResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerPricingCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerPricingQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pricing/price-lists")
@RequiredArgsConstructor
public class SellerPriceListController {

    private final SellerPricingCommandService commandService;
    private final SellerPricingQueryService queryService;
    private final PriceListModelAssembler modelAssembler;

    @PostMapping
    public ResponseEntity<EntityModel<PriceListResponse>> createPriceList(
            @Valid @RequestBody CreatePriceListRequest request) {
        PriceListResponse response = commandService.createPriceList(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(modelAssembler.toCommandModel(response));
    }

    @GetMapping("/{priceListId}")
    public ResponseEntity<EntityModel<PriceListResponse>> getPriceList(@PathVariable String priceListId) {
        return queryService.getPriceList(priceListId)
                .map(modelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<PriceListResponse>>> listPriceLists() {
        List<PriceListResponse> results = queryService.listPriceLists();
        return ResponseEntity.ok(modelAssembler.listPriceListsModel(results));
    }
}
