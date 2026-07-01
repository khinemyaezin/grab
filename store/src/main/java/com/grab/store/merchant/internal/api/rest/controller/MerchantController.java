package com.grab.store.merchant.internal.api.rest.controller;

import com.grab.store.merchant.internal.api.rest.assembler.C2CApplicationAssembler;
import com.grab.store.merchant.internal.api.rest.dto.request.StartMerchantApplicationRequest;
import com.grab.store.merchant.internal.api.rest.dto.request.UpdateMerchantProfileRequest;
import com.grab.store.merchant.internal.api.rest.dto.response.GetC2CApplicationResponse;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantResponse;
import com.grab.store.merchant.internal.api.rest.service.MerchantCommandService;
import com.grab.store.merchant.internal.api.rest.service.MerchantQueryService;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.shared.security.SecurityPrincipal;
import com.merchant.domain.enums.MerchantType;
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
    private final C2CApplicationAssembler c2CApplicationAssembler;

    @GetMapping("/applications/c2c")
    public ResponseEntity<EntityModel<GetC2CApplicationResponse>> getC2CApplication(
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String applicantId = principal.getPlatformUserId();
        GetC2CApplicationResponse response = queries.getC2CApplication(applicantId);
        return ResponseEntity.ok(c2CApplicationAssembler.toModel(response));
    }

    @PostMapping("/applications/c2c")
    public ResponseEntity<EntityModel<MerchantResponse>> startC2CApplication(
            @Valid @RequestBody StartMerchantApplicationRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String applicantId = principal.getPlatformUserId();
        MerchantResponse response = commands.start(request, MerchantType.C2C_SELLER, applicantId);
        EntityModel<MerchantResponse> model = assembler.toModel(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @PostMapping("/applications/first-party")
    public ResponseEntity<EntityModel<MerchantResponse>> startFirstPartyApplication(
            @Valid @RequestBody StartMerchantApplicationRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String applicantId = principal.getPlatformUserId();
        MerchantResponse response = commands.start(request, MerchantType.FIRST_PARTY_RETAILER, applicantId);
        EntityModel<MerchantResponse> model = assembler.toModel(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @PostMapping("/applications/third-party")
    public ResponseEntity<EntityModel<MerchantResponse>> startThirdPartyApplication(
            @Valid @RequestBody StartMerchantApplicationRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String applicantId = principal.getPlatformUserId();
        MerchantResponse response = commands.start(request, MerchantType.THIRD_PARTY, applicantId);
        EntityModel<MerchantResponse> model = assembler.toModel(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @GetMapping("/accounts")
    public ResponseEntity<CollectionModel<EntityModel<MerchantResponse>>> listMyMerchants(
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String applicantId = principal.getPlatformUserId();
        List<MerchantResponse> responses = queries.mine(applicantId);
        CollectionModel<EntityModel<MerchantResponse>> model = assembler.toCollectionModel(responses);
        return ResponseEntity.ok(model);
    }

    @GetMapping("/accounts/{merchantId}")
    public ResponseEntity<EntityModel<MerchantResponse>> getMerchant(
            @PathVariable String merchantId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        MerchantResponse response = queries.get(merchantId, principal);
        EntityModel<MerchantResponse> model = assembler.toModel(response);
        return ResponseEntity.ok(model);
    }

    @PatchMapping("/accounts/{merchantId}/profile")
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

    @PostMapping("/accounts/{merchantId}/submit")
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
