package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.port.rest.SellerRestApi;

import com.grab.app.platform.seller.internal.command.*;
import com.grab.app.platform.seller.internal.query.*;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.assembler.C2CApplicationAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.assembler.MerchantModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.request.StartMerchantApplicationRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.response.GetC2CApplicationResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.response.MerchantResponse;
import com.grab.app.platform.seller.internal.command.MerchantCommandService;
import com.grab.app.platform.seller.internal.query.MerchantQueryService;
import com.grab.app.account.merchant.internal.config.MerchantEnabled;
import com.grab.app.shared.security.SecurityPrincipal;
import com.merchant.domain.enums.MerchantType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@MerchantEnabled
@RequestMapping("/api/v1/merchants/applications/c2c")
@RequiredArgsConstructor
public class C2CApplicationController implements SellerRestApi {
    private final C2CApplicationAssembler c2CApplicationAssembler;
    private final MerchantModelAssembler merchantModelAssembler;
    private final MerchantCommandService commands;
    private final MerchantQueryService query;

    @GetMapping("/")
    public ResponseEntity<EntityModel<GetC2CApplicationResponse>> getC2CApplication(
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String applicantId = principal.getPlatformUserId();
        GetC2CApplicationResponse response = query.getC2CApplication(applicantId);
        return ResponseEntity.ok(c2CApplicationAssembler.toModel(response));
    }

    @PostMapping("/")
    public ResponseEntity<EntityModel<MerchantResponse>> startC2CApplication(
            @Valid @RequestBody StartMerchantApplicationRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String applicantId = principal.getPlatformUserId();
        MerchantResponse response = commands.start(request, MerchantType.C2C_SELLER, applicantId);
        EntityModel<MerchantResponse> model = merchantModelAssembler.toModel(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }
}
