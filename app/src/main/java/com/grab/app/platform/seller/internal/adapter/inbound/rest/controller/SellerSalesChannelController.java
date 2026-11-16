package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.SalesChannelModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.SalesChannelProvisionModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.ProvisionWebsiteChannelRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SalesChannelProvisionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SalesChannelResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerSalesChannelCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerSalesChannelQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sales-channels")
@RequiredArgsConstructor
public class SellerSalesChannelController {

    private final SellerSalesChannelCommandService commandService;
    private final SellerSalesChannelQueryService queryService;
    private final SalesChannelModelAssembler modelAssembler;
    private final SalesChannelProvisionModelAssembler provisionModelAssembler;

    @PostMapping("/merchant-website")
    public ResponseEntity<EntityModel<SalesChannelProvisionResponse>> provisionMerchantWebsiteChannel(
            @Valid @RequestBody ProvisionWebsiteChannelRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        SalesChannelProvisionResponse response = commandService.provisionMerchantWebsiteChannel(
                principal.requiredScopeId(), request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.salesChannelId())
                .toUri();
        return ResponseEntity.created(location).body(provisionModelAssembler.toModel(response));
    }

    @GetMapping("/{salesChannelId}")
    public ResponseEntity<EntityModel<SalesChannelResponse>> getChannel(
            @PathVariable String salesChannelId,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        return queryService.getChannelById(salesChannelId, principal.requiredScopeId())
                .map(modelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<SalesChannelResponse>>> listChannels(
            @AuthenticationPrincipal SellerSecurityPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<SalesChannelResponse> channels = queryService.listChannels(
                principal.requiredScopeId(), page, size);
        return ResponseEntity.ok(modelAssembler.listChannelsModel(channels, page, size));
    }
}
