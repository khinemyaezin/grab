package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.MerchantModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateMerchantProfileRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerMerchantCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerMerchantQueryService;
import com.grab.app.platform.seller.port.rest.SellerMerchantResource;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/merchants")
public class SellerMerchantController implements SellerMerchantResource {

    private final SellerMerchantCommandService commandService;
    private final SellerMerchantQueryService queryService;
    private final MerchantModelAssembler modelAssembler;

    public SellerMerchantController(
            SellerMerchantCommandService commandService,
            SellerMerchantQueryService queryService,
            MerchantModelAssembler modelAssembler) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.modelAssembler = modelAssembler;
    }

    @Override
    @GetMapping("/{merchantId}")
    public ResponseEntity<EntityModel<MerchantResponse>> getMerchant(
            @PathVariable String merchantId,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId) {
        return queryService.getMerchant(merchantId, actorId)
                .map(modelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<MerchantResponse>>> listMyMerchants(
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId) {
        List<MerchantResponse> results = queryService.listMyMerchants(actorId);
        List<EntityModel<MerchantResponse>> models = results.stream().map(modelAssembler::toModel).toList();
        return ResponseEntity.ok(CollectionModel.of(models));
    }

    @Override
    @PutMapping("/{merchantId}/profile")
    public ResponseEntity<EntityModel<MerchantResponse>> updateProfile(
            @PathVariable String merchantId,
            @Valid @RequestBody UpdateMerchantProfileRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId) {
        MerchantResponse response = commandService.updateProfile(merchantId, actorId, request);
        return ResponseEntity.ok(modelAssembler.toModel(response));
    }
}
