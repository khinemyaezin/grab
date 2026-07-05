package com.grab.store.merchant.internal.api.rest.controller;

import com.grab.store.merchant.internal.api.rest.assembler.FirstPartyRetailerApplicationAssembler;
import com.grab.store.merchant.internal.api.rest.assembler.MerchantModelAssembler;
import com.grab.store.merchant.internal.api.rest.dto.request.StartMerchantApplicationRequest;
import com.grab.store.merchant.internal.api.rest.dto.response.GetFirstPartyRetailerApplicationResponse;
import com.grab.store.merchant.internal.api.rest.dto.response.MerchantResponse;
import com.grab.store.merchant.internal.api.rest.service.MerchantCommandService;
import com.grab.store.merchant.internal.api.rest.service.MerchantQueryService;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.shared.security.SecurityPrincipal;
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
@RequestMapping("/api/v1/merchants/applications/first-party")
@RequiredArgsConstructor
public class FirstPartyRetailerApplicationController {
    private final MerchantModelAssembler merchantModelAssembler;
    private final FirstPartyRetailerApplicationAssembler firstPartyRetailerApplicationAssembler;
    private final MerchantCommandService commands;
    private final MerchantQueryService query;

    @GetMapping("/")
    public ResponseEntity<EntityModel<GetFirstPartyRetailerApplicationResponse>> getFirstPartyApplication(
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String applicantId = principal.getPlatformUserId();
        GetFirstPartyRetailerApplicationResponse response = query.getFirstPartyRetailerApplication(applicantId);
        return ResponseEntity.ok(firstPartyRetailerApplicationAssembler.toModel(response));
    }

    @PostMapping("/")
    public ResponseEntity<EntityModel<MerchantResponse>> startFirstPartyApplication(
            @Valid @RequestBody StartMerchantApplicationRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String applicantId = principal.getPlatformUserId();
        MerchantResponse response = commands.start(request, MerchantType.FIRST_PARTY_RETAILER, applicantId);
        EntityModel<MerchantResponse> model = merchantModelAssembler.toModel(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }
}
