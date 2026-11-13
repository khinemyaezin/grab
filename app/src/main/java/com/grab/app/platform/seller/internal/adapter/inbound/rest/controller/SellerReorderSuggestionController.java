package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.ReorderSuggestionModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.ReorderSuggestionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerInventoryQueryService;
import com.grab.app.platform.seller.port.rest.SellerInventoryResource;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/reorder-suggestions")
public class SellerReorderSuggestionController implements SellerInventoryResource.ReorderSuggestions {

    private final SellerInventoryQueryService queryService;
    private final ReorderSuggestionModelAssembler modelAssembler;

    public SellerReorderSuggestionController(
            SellerInventoryQueryService queryService,
            ReorderSuggestionModelAssembler modelAssembler) {
        this.queryService = queryService;
        this.modelAssembler = modelAssembler;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ReorderSuggestionResponse>>> listSuggestions(
            @RequestHeader(value = "X-Merchant-Id", required = false) String merchantId,
            @RequestParam(required = false) String locationId,
            @RequestParam(required = false) String sku) {
        List<ReorderSuggestionResponse> results = queryService.listReorderSuggestions(merchantId, locationId, sku);
        return ResponseEntity.ok(modelAssembler.listSuggestionsModel(results, locationId, sku));
    }
}
