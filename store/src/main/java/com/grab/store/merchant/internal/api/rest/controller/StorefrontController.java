package com.grab.store.merchant.internal.api.rest.controller;

import com.grab.store.merchant.internal.api.rest.assembler.StorefrontModelAssembler;
import com.grab.store.merchant.internal.api.rest.dto.request.CreateStorefrontRequest;
import com.grab.store.merchant.internal.api.rest.dto.request.MerchantLifecycleRequest;
import com.grab.store.merchant.internal.api.rest.dto.request.UpdateStorefrontProfileRequest;
import com.grab.store.merchant.internal.api.rest.dto.response.StorefrontResponse;
import com.grab.store.merchant.internal.api.rest.service.AuthenticatedMerchantScopeResolver;
import com.grab.store.merchant.internal.api.rest.service.StorefrontCommandService;
import com.grab.store.merchant.internal.api.rest.service.StorefrontQueryService;
import com.grab.store.merchant.internal.command.ChangeStorefrontLifecycleCommand.Action;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.shared.security.SecurityPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@MerchantEnabled
@RequestMapping("/api/v1/merchants/storefronts")
@RequiredArgsConstructor
public class StorefrontController {
    private final StorefrontCommandService commands;
    private final StorefrontQueryService queries;
    private final StorefrontModelAssembler assembler;
    private final AuthenticatedMerchantScopeResolver merchantScopes;

    @PostMapping
    public ResponseEntity<EntityModel<StorefrontResponse>> create(
            @Valid @RequestBody CreateStorefrontRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String merchantId = merchantScopes.resolveCurrentMerchantId(principal);
        StorefrontResponse response = commands.create(merchantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(response));
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<StorefrontResponse>>> list(
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String merchantId = merchantScopes.resolveCurrentMerchantId(principal);
        List<StorefrontResponse> responses = queries.list(merchantId);
        return ResponseEntity.ok(assembler.toCollectionModel(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<StorefrontResponse>> get(
            @PathVariable String id,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String merchantId = merchantScopes.resolveCurrentMerchantId(principal);
        StorefrontResponse response = queries.get(id, merchantId);
        return ResponseEntity.ok(assembler.toModel(response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EntityModel<StorefrontResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateStorefrontProfileRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String merchantId = merchantScopes.resolveCurrentMerchantId(principal);
        StorefrontResponse response = commands.update(id, merchantId, request);
        return ResponseEntity.ok(assembler.toModel(response));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<EntityModel<StorefrontResponse>> activate(
            @PathVariable String id,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        return change(id, principal, Action.ACTIVATE, null);
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<EntityModel<StorefrontResponse>> suspend(
            @PathVariable String id,
            @Valid @RequestBody MerchantLifecycleRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        return change(id, principal, Action.SUSPEND, request);
    }

    @PostMapping("/{id}/reactivate")
    public ResponseEntity<EntityModel<StorefrontResponse>> reactivate(
            @PathVariable String id,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        return change(id, principal, Action.REACTIVATE, null);
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<EntityModel<StorefrontResponse>> close(
            @PathVariable String id,
            @Valid @RequestBody MerchantLifecycleRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        return change(id, principal, Action.CLOSE, request);
    }

    private ResponseEntity<EntityModel<StorefrontResponse>> change(
            String id, SecurityPrincipal principal, Action action, MerchantLifecycleRequest request
    ) {
        String merchantId = merchantScopes.resolveCurrentMerchantId(principal);
        StorefrontResponse response = commands.changeLifecycle(id, merchantId, action, request);
        return ResponseEntity.ok(assembler.toModel(response));
    }
}
