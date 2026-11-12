package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.MerchantModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.MerchantLifecycleRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerMerchantAdminCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerMerchantAdminQueryService;
import com.grab.app.platform.seller.port.rest.SellerMerchantResource;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/identity/admin/merchants")
public class SellerMerchantAdminController implements SellerMerchantResource.Admin {

    private final SellerMerchantAdminCommandService commandService;
    private final SellerMerchantAdminQueryService queryService;
    private final MerchantModelAssembler modelAssembler;

    public SellerMerchantAdminController(
            SellerMerchantAdminCommandService commandService,
            SellerMerchantAdminQueryService queryService,
            MerchantModelAssembler modelAssembler
    ) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.modelAssembler = modelAssembler;
    }

    @Override
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<MerchantResponse>>> reviewQueue(
            @RequestParam(defaultValue = "PENDING_REVIEW") String status
    ) {
        List<MerchantResponse> responses = queryService.reviewQueue(status);
        List<EntityModel<MerchantResponse>> models = responses.stream().map(modelAssembler::toModel).toList();
        return ResponseEntity.ok(CollectionModel.of(models));
    }

    @Override
    @PostMapping("/{merchantId}/request-changes")
    public ResponseEntity<EntityModel<MerchantResponse>> requestChanges(
            @PathVariable String merchantId,
            @Valid @RequestBody MerchantLifecycleRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal
    ) {
        String actorId = principal != null ? principal.getPlatformUserId() : null;
        MerchantResponse response = commandService.requestChanges(merchantId, actorId, request.reason());
        return ResponseEntity.ok(modelAssembler.toModel(response));
    }

    @Override
    @PostMapping("/{merchantId}/approve")
    public ResponseEntity<EntityModel<MerchantResponse>> approve(
            @PathVariable String merchantId,
            @AuthenticationPrincipal SellerSecurityPrincipal principal
    ) {
        String actorId = principal != null ? principal.getPlatformUserId() : null;
        MerchantResponse response = commandService.approve(merchantId, actorId);
        return ResponseEntity.ok(modelAssembler.toModel(response));
    }

    @Override
    @PostMapping("/{merchantId}/reject")
    public ResponseEntity<EntityModel<MerchantResponse>> reject(
            @PathVariable String merchantId,
            @Valid @RequestBody MerchantLifecycleRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal
    ) {
        String actorId = principal != null ? principal.getPlatformUserId() : null;
        MerchantResponse response = commandService.reject(merchantId, actorId, request.reason());
        return ResponseEntity.ok(modelAssembler.toModel(response));
    }

    @Override
    @PostMapping("/{merchantId}/suspend")
    public ResponseEntity<EntityModel<MerchantResponse>> suspend(
            @PathVariable String merchantId,
            @Valid @RequestBody MerchantLifecycleRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal
    ) {
        String actorId = principal != null ? principal.getPlatformUserId() : null;
        MerchantResponse response = commandService.suspend(merchantId, actorId, request.reason());
        return ResponseEntity.ok(modelAssembler.toModel(response));
    }

    @Override
    @PostMapping("/{merchantId}/reactivate")
    public ResponseEntity<EntityModel<MerchantResponse>> reactivate(
            @PathVariable String merchantId,
            @AuthenticationPrincipal SellerSecurityPrincipal principal
    ) {
        String actorId = principal != null ? principal.getPlatformUserId() : null;
        MerchantResponse response = commandService.reactivate(merchantId, actorId);
        return ResponseEntity.ok(modelAssembler.toModel(response));
    }

    @Override
    @PostMapping("/{merchantId}/close")
    public ResponseEntity<EntityModel<MerchantResponse>> close(
            @PathVariable String merchantId,
            @Valid @RequestBody MerchantLifecycleRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal
    ) {
        String actorId = principal != null ? principal.getPlatformUserId() : null;
        MerchantResponse response = commandService.close(merchantId, actorId);
        return ResponseEntity.ok(modelAssembler.toModel(response));
    }
}

