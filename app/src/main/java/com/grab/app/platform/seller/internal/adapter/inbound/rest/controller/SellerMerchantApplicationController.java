package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.C2CApplicationAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.FirstPartyRetailerApplicationAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.MerchantModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.StartMerchantApplicationRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.GetC2CApplicationResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.GetFirstPartyRetailerApplicationResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerC2CApplicationCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerC2CApplicationQueryService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerFirstPartyRetailerApplicationCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerFirstPartyRetailerApplicationQueryService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerMerchantCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/merchants/applications")
@RequiredArgsConstructor
public class SellerMerchantApplicationController {

    private final SellerMerchantCommandService merchantCommandService;
    private final SellerC2CApplicationCommandService c2CApplicationCommandService;
    private final SellerC2CApplicationQueryService c2CApplicationQueryService;
    private final SellerFirstPartyRetailerApplicationCommandService firstPartyCommandService;
    private final SellerFirstPartyRetailerApplicationQueryService firstPartyQueryService;
    private final MerchantModelAssembler merchantModelAssembler;
    private final C2CApplicationAssembler c2CApplicationAssembler;
    private final FirstPartyRetailerApplicationAssembler firstPartyRetailerApplicationAssembler;

    @PostMapping
    public ResponseEntity<EntityModel<MerchantResponse>> startApplication(
            @Valid @RequestBody StartMerchantApplicationRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        MerchantResponse response = merchantCommandService.startApplication(
                principal.getPlatformUserId(), request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.merchantId())
                .toUri();
        return ResponseEntity.created(location).body(merchantModelAssembler.toCommandModel(response));
    }

    @PutMapping("/{applicationId}/submit")
    public ResponseEntity<EntityModel<MerchantResponse>> submitApplication(
            @PathVariable String applicationId,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        MerchantResponse response = merchantCommandService.submitApplication(
                applicationId, principal.getPlatformUserId());
        return ResponseEntity.ok(merchantModelAssembler.toCommandModel(response));
    }

    @GetMapping("/c2c")
    public ResponseEntity<EntityModel<GetC2CApplicationResponse>> getC2CApplication(
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        GetC2CApplicationResponse response = c2CApplicationQueryService
                .getC2CApplication(principal.getPlatformUserId());
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(c2CApplicationAssembler.toModel(response));
    }

    @PostMapping("/c2c")
    public ResponseEntity<EntityModel<MerchantResponse>> startC2CApplication(
            @Valid @RequestBody StartMerchantApplicationRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        MerchantResponse response = c2CApplicationCommandService.startC2CApplication(
                principal.getPlatformUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(merchantModelAssembler.toCommandModel(response));
    }

    @GetMapping("/first-party")
    public ResponseEntity<EntityModel<GetFirstPartyRetailerApplicationResponse>> getFirstPartyApplication(
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        GetFirstPartyRetailerApplicationResponse response = firstPartyQueryService
                .getFirstPartyApplication(principal.getPlatformUserId());
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(firstPartyRetailerApplicationAssembler.toModel(response));
    }

    @PostMapping("/first-party")
    public ResponseEntity<EntityModel<MerchantResponse>> startFirstPartyApplication(
            @Valid @RequestBody StartMerchantApplicationRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        MerchantResponse response = firstPartyCommandService.startFirstPartyApplication(
                principal.getPlatformUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(merchantModelAssembler.toCommandModel(response));
    }
}
