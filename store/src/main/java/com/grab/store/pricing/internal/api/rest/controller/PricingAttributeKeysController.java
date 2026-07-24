package com.grab.store.pricing.internal.api.rest.controller;

import com.grab.store.pricing.internal.api.rest.dto.response.PricingAttributeKeysResponse;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.pricing.domain.valueobject.PricingAttributeKeys;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@PricingEnabled
@RequestMapping("/api/v1/pricing/attribute-keys")
public class PricingAttributeKeysController {

    @GetMapping
    public ResponseEntity<EntityModel<PricingAttributeKeysResponse>> listAttributeKeys() {
        PricingAttributeKeysResponse response = new PricingAttributeKeysResponse(
                PricingAttributeKeys.wellKnown(),
                PricingAttributeKeys.taxPreferenceLookupOrder()
        );
        EntityModel<PricingAttributeKeysResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(PricingAttributeKeysController.class).listAttributeKeys()).withSelfRel());
        return ResponseEntity.ok(model);
    }
}
