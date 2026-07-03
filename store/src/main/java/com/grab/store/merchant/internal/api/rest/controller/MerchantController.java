package com.grab.store.merchant.internal.api.rest.controller;

import com.grab.store.merchant.internal.api.rest.dto.request.UpdateMerchantProfileRequest;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantResponse;
import com.grab.store.merchant.internal.api.rest.service.MerchantCommandService;
import com.grab.store.merchant.internal.api.rest.service.MerchantQueryService;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.shared.security.SecurityPrincipal;
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
@RequestMapping("/api/v1/merchants/accounts")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantCommandService commands;
    private final MerchantQueryService queries;
    private final MerchantModelAssembler assembler;

    @GetMapping()
    public ResponseEntity<CollectionModel<EntityModel<MerchantResponse>>> getMerchants(
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        List<MerchantResponse> responses = queries.mine(principal.getPlatformUserId());
        CollectionModel<EntityModel<MerchantResponse>> collection = assembler.toCollectionModel(responses);
        return ResponseEntity.ok(collection);
    }

    @GetMapping("/{merchantId}")
    public ResponseEntity<EntityModel<MerchantResponse>> getMerchant(
            @PathVariable String merchantId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        MerchantResponse response = queries.get(merchantId, principal);
        EntityModel<MerchantResponse> model = assembler.toModel(response);
        return ResponseEntity.ok(model);
    }

    @PatchMapping("/{merchantId}/profile")
    public ResponseEntity<EntityModel<MerchantResponse>> update(
            @PathVariable String merchantId,
            @Valid @RequestBody UpdateMerchantProfileRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String applicantId = principal.getPlatformUserId();
        MerchantResponse response = commands.update(merchantId, request, applicantId);
        EntityModel<MerchantResponse> model = assembler.toModel(response);
        return ResponseEntity.ok(model);
    }

    @PostMapping("/{merchantId}/submit")
    public ResponseEntity<EntityModel<MerchantResponse>> submit(
            @PathVariable String merchantId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String applicantId = principal.getPlatformUserId();
        MerchantResponse response = commands.submit(merchantId, applicantId);
        EntityModel<MerchantResponse> model = assembler.toModel(response);
        return ResponseEntity.ok(model);
    }
}
