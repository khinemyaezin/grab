package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.PriceListModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreatePriceListRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PriceListResponse;
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
@RequestMapping("/api/seller/v1/pricing/price-lists")
public class SellerPriceListController implements SellerPriceListResource {

    private final SellerPricingCommandService commandService;
    private final SellerPricingQueryService queryService;
    private final PriceListModelAssembler modelAssembler;

    public SellerPriceListController(
            SellerPricingCommandService commandService,
            SellerPricingQueryService queryService,
            PriceListModelAssembler modelAssembler) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.modelAssembler = modelAssembler;
    }

    @PostMapping
    public ResponseEntity<EntityModel<PriceListResponse>> createPriceList(@Valid @RequestBody CreatePriceListRequest request) {
        PriceListResponse response = commandService.createPriceList(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(modelAssembler.toModel(response));
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
        List<EntityModel<PriceListResponse>> models = results.stream().map(modelAssembler::toModel).toList();
        return ResponseEntity.ok(CollectionModel.of(models));
    }
}
