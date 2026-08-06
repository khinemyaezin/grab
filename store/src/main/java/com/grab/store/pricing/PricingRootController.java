package com.grab.store.pricing;

import com.grab.store.pricing.api.PricingApiLinks;
import com.grab.store.pricing.internal.api.rest.controller.PriceListController;
import com.grab.store.pricing.internal.api.rest.controller.PricePreferenceController;
import com.grab.store.pricing.internal.api.rest.controller.PriceSetController;
import com.grab.store.pricing.internal.api.rest.controller.PricingAttributeKeysController;
import com.grab.store.pricing.internal.config.PricingEnabled;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@PricingEnabled
@RequestMapping("/api/v1/pricing")
public class PricingRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(linkTo(methodOn(PricingRootController.class).root()).withSelfRel());
        model.add(linkTo(methodOn(PriceSetController.class).createPriceSet()).withRel("create-price-set"));
        model.add(PricingApiLinks.calculatePrices());
        model.add(linkTo(methodOn(PriceListController.class).listPriceLists()).withRel("list-price-lists"));
        model.add(linkTo(methodOn(PriceListController.class).createPriceList(null)).withRel("create-price-list"));
        model.add(linkTo(methodOn(PricePreferenceController.class).listPreferences())
                .withRel("list-price-preferences"));
        model.add(linkTo(methodOn(PricePreferenceController.class).createPreference(null))
                .withRel("create-price-preference"));
        model.add(linkTo(methodOn(PricingAttributeKeysController.class).listAttributeKeys())
                .withRel("list-pricing-attribute-keys"));
        model.add(PricingApiLinks.listVariantPriceLinks());
        return ResponseEntity.ok(model);
    }
}
