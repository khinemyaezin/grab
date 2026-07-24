package com.grab.store.pricing.internal.api.rest.controller;

import com.grab.store.pricing.internal.api.rest.assembler.PricePreferenceModelAssembler;
import com.grab.store.pricing.internal.api.rest.dto.request.CreatePricePreferenceRequest;
import com.grab.store.pricing.internal.api.rest.dto.request.UpdatePricePreferenceRequest;
import com.grab.store.pricing.internal.api.rest.dto.response.PricePreferenceResponse;
import com.grab.store.pricing.internal.api.rest.service.PricePreferenceCommandService;
import com.grab.store.pricing.internal.api.rest.service.PricePreferenceQueryService;
import com.grab.store.pricing.internal.config.PricingEnabled;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@PricingEnabled
@RequiredArgsConstructor
@RequestMapping("/api/v1/pricing/price-preferences")
public class PricePreferenceController {

    private final PricePreferenceCommandService pricePreferenceCommandService;
    private final PricePreferenceQueryService pricePreferenceQueryService;
    private final PricePreferenceModelAssembler pricePreferenceModelAssembler;

    @PostMapping
    public ResponseEntity<EntityModel<PricePreferenceResponse>> createPreference(
            @Valid @RequestBody CreatePricePreferenceRequest request
    ) {
        PricePreferenceResponse response = pricePreferenceCommandService.create(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(pricePreferenceModelAssembler.toModel(response));
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<PricePreferenceResponse>>> listPreferences() {
        List<EntityModel<PricePreferenceResponse>> models = pricePreferenceQueryService.list().stream()
                .map(pricePreferenceModelAssembler::toModel)
                .toList();
        CollectionModel<EntityModel<PricePreferenceResponse>> collection = CollectionModel.of(models);
        collection.add(linkTo(methodOn(PricePreferenceController.class).listPreferences())
                .withRel("list-price-preferences"));
        collection.add(linkTo(methodOn(PricePreferenceController.class).createPreference(null))
                .withRel("create-price-preference"));
        return ResponseEntity.ok(collection);
    }

    @GetMapping("/{pricePreferenceId}")
    public ResponseEntity<EntityModel<PricePreferenceResponse>> getPreference(
            @PathVariable String pricePreferenceId
    ) {
        PricePreferenceResponse response = pricePreferenceQueryService.get(pricePreferenceId);
        return ResponseEntity.ok(pricePreferenceModelAssembler.toModel(response));
    }

    @PutMapping("/{pricePreferenceId}")
    public ResponseEntity<EntityModel<PricePreferenceResponse>> updatePreference(
            @PathVariable String pricePreferenceId,
            @Valid @RequestBody UpdatePricePreferenceRequest request
    ) {
        PricePreferenceResponse response = pricePreferenceCommandService.update(pricePreferenceId, request);
        return ResponseEntity.ok(pricePreferenceModelAssembler.toModel(response));
    }

    @DeleteMapping("/{pricePreferenceId}")
    public ResponseEntity<Void> deletePreference(@PathVariable String pricePreferenceId) {
        pricePreferenceCommandService.delete(pricePreferenceId);
        return ResponseEntity.noContent().build();
    }
}
