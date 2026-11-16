package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.PricePreferenceModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreatePricePreferenceRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PricePreferenceResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerPricingCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerPricingQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/pricing/preferences")
@RequiredArgsConstructor
public class SellerPricePreferenceController {

    private final SellerPricingCommandService commandService;
    private final SellerPricingQueryService queryService;
    private final PricePreferenceModelAssembler modelAssembler;

    @PostMapping
    public ResponseEntity<EntityModel<PricePreferenceResponse>> createPreference(
            @Valid @RequestBody CreatePricePreferenceRequest request) {
        PricePreferenceResponse response = commandService.createPricePreference(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(modelAssembler.toCommandModel(response));
    }

    @GetMapping("/{preferenceId}")
    public ResponseEntity<EntityModel<PricePreferenceResponse>> getPreference(
            @PathVariable String preferenceId) {
        return queryService.getPricePreference(preferenceId)
                .map(modelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
