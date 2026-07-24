package com.grab.store.pricing.internal.api.rest.controller;

import com.grab.store.pricing.internal.api.rest.assembler.PriceListModelAssembler;
import com.grab.store.pricing.internal.api.rest.dto.request.AddPriceListPriceRequest;
import com.grab.store.pricing.internal.api.rest.dto.request.CreatePriceListRequest;
import com.grab.store.pricing.internal.api.rest.dto.request.ReplacePriceListRulesRequest;
import com.grab.store.pricing.internal.api.rest.dto.request.UpdatePriceListRequest;
import com.grab.store.pricing.internal.api.rest.dto.response.PriceListResponse;
import com.grab.store.pricing.internal.api.rest.service.PriceListCommandService;
import com.grab.store.pricing.internal.api.rest.service.PriceListQueryService;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@PricingEnabled
@RequiredArgsConstructor
@RequestMapping("/api/v1/pricing/price-lists")
public class PriceListController {

    private final PriceListCommandService priceListCommandService;
    private final PriceListQueryService priceListQueryService;
    private final PriceListModelAssembler priceListModelAssembler;

    @PostMapping
    public ResponseEntity<EntityModel<PriceListResponse>> createPriceList(
            @Valid @RequestBody CreatePriceListRequest request
    ) {
        PriceListResponse response = priceListCommandService.createPriceList(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(priceListModelAssembler.toModel(response));
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<PriceListResponse>>> listPriceLists() {
        List<EntityModel<PriceListResponse>> models = priceListQueryService.listPriceLists().stream()
                .map(priceListModelAssembler::toModel)
                .toList();
        CollectionModel<EntityModel<PriceListResponse>> collection = CollectionModel.of(models);
        collection.add(linkTo(methodOn(PriceListController.class).listPriceLists()).withRel("list-price-lists"));
        collection.add(linkTo(methodOn(PriceListController.class).createPriceList(null)).withRel("create-price-list"));
        return ResponseEntity.ok(collection);
    }

    @GetMapping("/{priceListId}")
    public ResponseEntity<EntityModel<PriceListResponse>> getPriceList(@PathVariable String priceListId) {
        PriceListResponse response = priceListQueryService.getPriceList(priceListId);
        return ResponseEntity.ok(priceListModelAssembler.toModel(response));
    }

    @PutMapping("/{priceListId}")
    public ResponseEntity<EntityModel<PriceListResponse>> updatePriceList(
            @PathVariable String priceListId,
            @Valid @RequestBody UpdatePriceListRequest request
    ) {
        PriceListResponse response = priceListCommandService.updatePriceList(priceListId, request);
        return ResponseEntity.ok(priceListModelAssembler.toModel(response));
    }

    @PutMapping("/{priceListId}/rules")
    public ResponseEntity<EntityModel<PriceListResponse>> replaceRules(
            @PathVariable String priceListId,
            @Valid @RequestBody ReplacePriceListRulesRequest request
    ) {
        PriceListResponse response = priceListCommandService.replaceRules(priceListId, request);
        return ResponseEntity.ok(priceListModelAssembler.toModel(response));
    }

    @PostMapping("/{priceListId}/prices")
    public ResponseEntity<EntityModel<PriceListResponse>> addPrice(
            @PathVariable String priceListId,
            @Valid @RequestBody AddPriceListPriceRequest request
    ) {
        PriceListResponse response = priceListCommandService.addPrice(priceListId, request);
        return ResponseEntity.ok(priceListModelAssembler.toModel(response));
    }

    @DeleteMapping("/{priceListId}/prices/{priceId}")
    public ResponseEntity<EntityModel<PriceListResponse>> removePrice(
            @PathVariable String priceListId,
            @PathVariable String priceId
    ) {
        PriceListResponse response = priceListCommandService.removePrice(priceListId, priceId);
        return ResponseEntity.ok(priceListModelAssembler.toModel(response));
    }

    @DeleteMapping("/{priceListId}")
    public ResponseEntity<Void> deletePriceList(@PathVariable String priceListId) {
        priceListCommandService.deletePriceList(priceListId);
        return ResponseEntity.noContent().build();
    }
}
