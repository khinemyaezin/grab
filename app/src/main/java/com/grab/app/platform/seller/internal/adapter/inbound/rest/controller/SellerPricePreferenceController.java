package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.PricePreferenceModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreatePricePreferenceRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PricePreferenceResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerPricingCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerPricingQueryService;
import com.grab.app.platform.seller.port.rest.SellerPriceListResource;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/pricing/preferences")
public class SellerPricePreferenceController implements SellerPriceListResource.Preferences {

    private final SellerPricingCommandService commandService;
    private final SellerPricingQueryService queryService;
    private final PricePreferenceModelAssembler modelAssembler;

    public SellerPricePreferenceController(
            SellerPricingCommandService commandService,
            SellerPricingQueryService queryService,
            PricePreferenceModelAssembler modelAssembler) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.modelAssembler = modelAssembler;
    }

    @PostMapping
    public ResponseEntity<EntityModel<PricePreferenceResponse>> createPreference(@Valid @RequestBody CreatePricePreferenceRequest request) {
        PricePreferenceResponse response = commandService.createPricePreference(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(modelAssembler.toModel(response));
    }

    @GetMapping("/{preferenceId}")
    public ResponseEntity<EntityModel<PricePreferenceResponse>> getPreference(@PathVariable String preferenceId) {
        return queryService.getPricePreference(preferenceId)
                .map(modelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
