package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.MerchantModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateMerchantProfileRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerMerchantCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerMerchantQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class SellerMerchantController {

    private final SellerMerchantCommandService commandService;
    private final SellerMerchantQueryService queryService;
    private final MerchantModelAssembler modelAssembler;

    @GetMapping("/{merchantId}")
    public ResponseEntity<EntityModel<MerchantResponse>> getMerchant(
            @PathVariable String merchantId,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        return queryService.getMerchant(merchantId, principal.getPlatformUserId())
                .map(modelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<MerchantResponse>>> listMyMerchants(
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        List<MerchantResponse> results = queryService.listMyMerchants(principal.getPlatformUserId());
        return ResponseEntity.ok(modelAssembler.listMyMerchantsModel(results));
    }

    @PatchMapping("/{merchantId}/profile")
    public ResponseEntity<EntityModel<MerchantResponse>> updateProfile(
            @PathVariable String merchantId,
            @Valid @RequestBody UpdateMerchantProfileRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        MerchantResponse response = commandService.updateProfile(
                merchantId, principal.getPlatformUserId(), request);
        return ResponseEntity.ok(modelAssembler.toCommandModel(response));
    }
}
