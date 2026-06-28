package com.grab.store.merchant.internal.api.rest.controller;

import com.grab.store.merchant.internal.api.rest.dto.request.StartMerchantApplicationRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@MerchantEnabled
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {
    private final MerchantCommandService commands;
    private final MerchantQueryService queries;
    private final MerchantModelAssembler assembler;

    @PostMapping("/onboading")
    public ResponseEntity<EntityModel<MerchantResponse>> start(
            @Valid @RequestBody StartMerchantApplicationRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String applicantId = principal.getPlatformUserId();
        MerchantResponse response = commands.start(request, applicantId);
        EntityModel<MerchantResponse> model = assembler.toModel(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<MerchantResponse>>> listMyMerchants(
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String applicantId = principal.getPlatformUserId();
        List<MerchantResponse> responses = queries.mine(applicantId);
        CollectionModel<EntityModel<MerchantResponse>> model = assembler.toCollectionModel(responses);
        return ResponseEntity.ok(model);
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
