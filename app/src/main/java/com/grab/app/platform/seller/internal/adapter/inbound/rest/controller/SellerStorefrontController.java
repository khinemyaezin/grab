package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.StorefrontModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateStorefrontRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateStorefrontRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.StorefrontResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerMerchantCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerMerchantQueryService;
import com.grab.app.platform.seller.port.rest.SellerMerchantResource;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/merchants/{merchantId}/storefronts")
public class SellerStorefrontController implements SellerMerchantResource.Storefronts {

    private final SellerMerchantCommandService commandService;
    private final SellerMerchantQueryService queryService;
    private final StorefrontModelAssembler modelAssembler;

    public SellerStorefrontController(
            SellerMerchantCommandService commandService,
            SellerMerchantQueryService queryService,
            StorefrontModelAssembler modelAssembler) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.modelAssembler = modelAssembler;
    }

    @PostMapping
    public ResponseEntity<EntityModel<StorefrontResponse>> createStorefront(
            @PathVariable String merchantId,
            @Valid @RequestBody CreateStorefrontRequest request) {
        StorefrontResponse response = commandService.createStorefront(merchantId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.storefrontId())
                .toUri();
        return ResponseEntity.created(location).body(modelAssembler.toModel(response));
    }

    @GetMapping("/{storefrontId}")
    public ResponseEntity<EntityModel<StorefrontResponse>> getStorefront(
            @PathVariable String merchantId,
            @PathVariable String storefrontId) {
        return queryService.getStorefront(merchantId, storefrontId)
                .map(modelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<StorefrontResponse>>> listStorefronts(@PathVariable String merchantId) {
        List<StorefrontResponse> results = queryService.listStorefronts(merchantId);
        List<EntityModel<StorefrontResponse>> models = results.stream().map(modelAssembler::toModel).toList();
        return ResponseEntity.ok(CollectionModel.of(models));
    }

    @PutMapping("/{storefrontId}")
    public ResponseEntity<EntityModel<StorefrontResponse>> updateStorefront(
            @PathVariable String merchantId,
            @PathVariable String storefrontId,
            @Valid @RequestBody UpdateStorefrontRequest request) {
        StorefrontResponse response = commandService.updateStorefront(merchantId, storefrontId, request);
        return ResponseEntity.ok(modelAssembler.toModel(response));
    }

    @PatchMapping("/{storefrontId}/activate")
    public ResponseEntity<Void> activateStorefront(@PathVariable String merchantId, @PathVariable String storefrontId) {
        commandService.activateStorefront(merchantId, storefrontId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{storefrontId}/close")
    public ResponseEntity<Void> closeStorefront(@PathVariable String merchantId, @PathVariable String storefrontId) {
        commandService.closeStorefront(merchantId, storefrontId);
        return ResponseEntity.noContent().build();
    }
}
