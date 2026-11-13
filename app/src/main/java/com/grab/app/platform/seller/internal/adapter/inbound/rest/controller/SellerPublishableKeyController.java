package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.PublishableKeyModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreatePublishableKeyRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.KeyResolutionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PublishableKeyResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerSalesChannelCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerSalesChannelQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/sales-channels")
@RequiredArgsConstructor
public class SellerPublishableKeyController {

    private final SellerSalesChannelCommandService commandService;
    private final SellerSalesChannelQueryService queryService;
    private final PublishableKeyModelAssembler modelAssembler;

    @PostMapping("/{channelId}/publishable-keys")
    public ResponseEntity<EntityModel<PublishableKeyResponse>> createPublishableKey(
            @PathVariable String channelId,
            @Valid @RequestBody CreatePublishableKeyRequest request) {
        PublishableKeyResponse response = commandService.createPublishableKey(channelId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.keyId())
                .toUri();
        return ResponseEntity.created(location).body(modelAssembler.toModel(response));
    }

    @DeleteMapping("/{channelId}/publishable-keys/{keyId}")
    public ResponseEntity<Void> revokePublishableKey(
            @PathVariable String channelId,
            @PathVariable String keyId) {
        commandService.revokePublishableKey(channelId, keyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/publishable-keys/resolve")
    public ResponseEntity<KeyResolutionResponse> resolveKey(@RequestParam String token) {
        return queryService.resolveKey(token)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
