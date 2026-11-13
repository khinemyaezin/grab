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
import com.grab.app.platform.seller.port.rest.SellerMerchantResource;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/merchants/applications")
public class SellerMerchantApplicationController implements SellerMerchantResource.Applications {

    private final SellerMerchantCommandService merchantCommandService;
    private final SellerC2CApplicationCommandService c2CApplicationCommandService;
    private final SellerC2CApplicationQueryService c2CApplicationQueryService;
    private final SellerFirstPartyRetailerApplicationCommandService firstPartyCommandService;
    private final SellerFirstPartyRetailerApplicationQueryService firstPartyQueryService;
    private final MerchantModelAssembler merchantModelAssembler;
    private final C2CApplicationAssembler c2CApplicationAssembler;
    private final FirstPartyRetailerApplicationAssembler firstPartyRetailerApplicationAssembler;

    public SellerMerchantApplicationController(
            SellerMerchantCommandService merchantCommandService,
            SellerC2CApplicationCommandService c2CApplicationCommandService,
            SellerC2CApplicationQueryService c2CApplicationQueryService,
            SellerFirstPartyRetailerApplicationCommandService firstPartyCommandService,
            SellerFirstPartyRetailerApplicationQueryService firstPartyQueryService,
            MerchantModelAssembler merchantModelAssembler,
            C2CApplicationAssembler c2CApplicationAssembler,
            FirstPartyRetailerApplicationAssembler firstPartyRetailerApplicationAssembler
    ) {
        this.merchantCommandService = merchantCommandService;
        this.c2CApplicationCommandService = c2CApplicationCommandService;
        this.c2CApplicationQueryService = c2CApplicationQueryService;
        this.firstPartyCommandService = firstPartyCommandService;
        this.firstPartyQueryService = firstPartyQueryService;
        this.merchantModelAssembler = merchantModelAssembler;
        this.c2CApplicationAssembler = c2CApplicationAssembler;
        this.firstPartyRetailerApplicationAssembler = firstPartyRetailerApplicationAssembler;
    }

    @Override
    @PostMapping
    public ResponseEntity<EntityModel<MerchantResponse>> startApplication(
            @Valid @RequestBody StartMerchantApplicationRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        MerchantResponse response = merchantCommandService.startApplication(actorId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.merchantId())
                .toUri();
        return ResponseEntity.created(location).body(merchantModelAssembler.toCommandModel(response));
    }

    @Override
    @PutMapping("/{applicationId}/submit")
    public ResponseEntity<EntityModel<MerchantResponse>> submitApplication(
            @PathVariable String applicationId,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        MerchantResponse response = merchantCommandService.submitApplication(applicationId, actorId);
        return ResponseEntity.ok(merchantModelAssembler.toCommandModel(response));
    }

    @Override
    @GetMapping("/c2c")
    public ResponseEntity<EntityModel<GetC2CApplicationResponse>> getC2CApplication(
            @AuthenticationPrincipal SellerSecurityPrincipal principal
    ) {
        String applicantId = principal != null ? principal.getPlatformUserId() : null;
        GetC2CApplicationResponse response = c2CApplicationQueryService.getC2CApplication(applicantId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(c2CApplicationAssembler.toModel(response));
    }

    @Override
    @PostMapping("/c2c")
    public ResponseEntity<EntityModel<MerchantResponse>> startC2CApplication(
            @Valid @RequestBody StartMerchantApplicationRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal
    ) {
        String applicantId = principal != null ? principal.getPlatformUserId() : null;
        MerchantResponse response = c2CApplicationCommandService.startC2CApplication(applicantId, request);
        EntityModel<MerchantResponse> model = merchantModelAssembler.toCommandModel(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @Override
    @GetMapping("/first-party")
    public ResponseEntity<EntityModel<GetFirstPartyRetailerApplicationResponse>> getFirstPartyApplication(
            @AuthenticationPrincipal SellerSecurityPrincipal principal
    ) {
        String applicantId = principal != null ? principal.getPlatformUserId() : null;
        GetFirstPartyRetailerApplicationResponse response = firstPartyQueryService.getFirstPartyApplication(applicantId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(firstPartyRetailerApplicationAssembler.toModel(response));
    }

    @Override
    @PostMapping("/first-party")
    public ResponseEntity<EntityModel<MerchantResponse>> startFirstPartyApplication(
            @Valid @RequestBody StartMerchantApplicationRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal
    ) {
        String applicantId = principal != null ? principal.getPlatformUserId() : null;
        MerchantResponse response = firstPartyCommandService.startFirstPartyApplication(applicantId, request);
        EntityModel<MerchantResponse> model = merchantModelAssembler.toCommandModel(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }
}

