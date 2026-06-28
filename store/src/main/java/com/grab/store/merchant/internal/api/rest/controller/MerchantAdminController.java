package com.grab.store.merchant.internal.api.rest.controller;

import com.grab.store.merchant.internal.api.rest.dto.request.MerchantLifecycleRequest;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantResponse;
import com.grab.store.merchant.internal.api.rest.service.MerchantCommandService;
import com.grab.store.merchant.internal.api.rest.service.MerchantQueryService;
import com.grab.store.merchant.internal.command.ChangeMerchantLifecycleCommand.Action;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.shared.security.SecurityPrincipal;
import com.merchant.domain.enums.MerchantStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.grab.store.merchant.internal.api.rest.assembler.MerchantModelAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@MerchantEnabled
@RequestMapping("/api/v1/admin/merchants")
@RequiredArgsConstructor
public class MerchantAdminController {
    private final MerchantCommandService commands;
    private final MerchantQueryService queries;
    private final MerchantModelAssembler assembler;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<MerchantResponse>>> reviewQueue(
            @RequestParam(defaultValue = "PENDING_REVIEW") MerchantStatus status
    ) {
        List<MerchantResponse> responses = queries.reviewQueue(status);
        CollectionModel<EntityModel<MerchantResponse>> model = assembler.toCollectionModel(responses);
        return ResponseEntity.ok(model);
    }

    @PostMapping("/{merchantId}/request-changes")
    public ResponseEntity<EntityModel<MerchantResponse>> requestChanges(
            @PathVariable String merchantId,
            @Valid @RequestBody MerchantLifecycleRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        return change(merchantId, principal, Action.REQUEST_CHANGES, request.reason());
    }

    @PostMapping("/{merchantId}/approve")
    public ResponseEntity<EntityModel<MerchantResponse>> approve(
            @PathVariable String merchantId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        return change(merchantId, principal, Action.APPROVE, null);
    }

    @PostMapping("/{merchantId}/reject")
    public ResponseEntity<EntityModel<MerchantResponse>> reject(
            @PathVariable String merchantId,
            @Valid @RequestBody MerchantLifecycleRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        return change(merchantId, principal, Action.REJECT, request.reason());
    }

    @PostMapping("/{merchantId}/suspend")
    public ResponseEntity<EntityModel<MerchantResponse>> suspend(
            @PathVariable String merchantId,
            @Valid @RequestBody MerchantLifecycleRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        return change(merchantId, principal, Action.SUSPEND, request.reason());
    }

    @PostMapping("/{merchantId}/reactivate")
    public ResponseEntity<EntityModel<MerchantResponse>> reactivate(
            @PathVariable String merchantId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        return change(merchantId, principal, Action.REACTIVATE, null);
    }

    @PostMapping("/{merchantId}/close")
    public ResponseEntity<EntityModel<MerchantResponse>> close(
            @PathVariable String merchantId,
            @Valid @RequestBody MerchantLifecycleRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        return change(merchantId, principal, Action.CLOSE, request.reason());
    }

    private ResponseEntity<EntityModel<MerchantResponse>> change(
            String merchantId, SecurityPrincipal principal, Action action, String reason
    ) {
        String actorId = principal.getPlatformUserId();
        MerchantResponse response = commands.changeLifecycle(merchantId, actorId, action, reason);
        EntityModel<MerchantResponse> model = assembler.toModel(response);
        return ResponseEntity.ok(model);
    }
}
