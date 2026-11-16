package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.MerchantModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.MerchantLifecycleRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerMerchantAdminCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerMerchantAdminQueryService;
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
@RequestMapping("/api/v1/identity/admin/merchants")
@RequiredArgsConstructor
public class SellerMerchantAdminController {

    private final SellerMerchantAdminCommandService commandService;
    private final SellerMerchantAdminQueryService queryService;
    private final MerchantModelAssembler modelAssembler;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<MerchantResponse>>> reviewQueue(
            @RequestParam(defaultValue = "PENDING_REVIEW") String status) {
        List<MerchantResponse> responses = queryService.reviewQueue(status);
        return ResponseEntity.ok(modelAssembler.reviewQueueModel(responses, status));
    }

    @PostMapping("/{merchantId}/request-changes")
    public ResponseEntity<EntityModel<MerchantResponse>> requestChanges(
            @PathVariable String merchantId,
            @Valid @RequestBody MerchantLifecycleRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        MerchantResponse response = commandService.requestChanges(
                merchantId, principal.getPlatformUserId(), request.reason());
        return ResponseEntity.ok(modelAssembler.toCommandModel(response));
    }

    @PostMapping("/{merchantId}/approve")
    public ResponseEntity<EntityModel<MerchantResponse>> approve(
            @PathVariable String merchantId,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        MerchantResponse response = commandService.approve(merchantId, principal.getPlatformUserId());
        return ResponseEntity.ok(modelAssembler.toCommandModel(response));
    }

    @PostMapping("/{merchantId}/reject")
    public ResponseEntity<EntityModel<MerchantResponse>> reject(
            @PathVariable String merchantId,
            @Valid @RequestBody MerchantLifecycleRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        MerchantResponse response = commandService.reject(
                merchantId, principal.getPlatformUserId(), request.reason());
        return ResponseEntity.ok(modelAssembler.toCommandModel(response));
    }

    @PostMapping("/{merchantId}/suspend")
    public ResponseEntity<EntityModel<MerchantResponse>> suspend(
            @PathVariable String merchantId,
            @Valid @RequestBody MerchantLifecycleRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        MerchantResponse response = commandService.suspend(
                merchantId, principal.getPlatformUserId(), request.reason());
        return ResponseEntity.ok(modelAssembler.toCommandModel(response));
    }

    @PostMapping("/{merchantId}/reactivate")
    public ResponseEntity<EntityModel<MerchantResponse>> reactivate(
            @PathVariable String merchantId,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        MerchantResponse response = commandService.reactivate(merchantId, principal.getPlatformUserId());
        return ResponseEntity.ok(modelAssembler.toCommandModel(response));
    }

    @PostMapping("/{merchantId}/close")
    public ResponseEntity<EntityModel<MerchantResponse>> close(
            @PathVariable String merchantId,
            @Valid @RequestBody MerchantLifecycleRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        MerchantResponse response = commandService.close(
                merchantId, principal.getPlatformUserId());
        return ResponseEntity.ok(modelAssembler.toCommandModel(response));
    }
}
