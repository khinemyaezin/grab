package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pricing")
public class SellerPricingRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(SellerDiscoveryApiLinks.pricingRoot().withSelfRel());

        model.add(SellerPricingApiLinks.listPriceLists());
        model.add(SellerPricingApiLinks.createPriceList());

        model.add(SellerPricingApiLinks.createPriceSet());
        model.add(SellerPricingApiLinks.calculatePrices());

        model.add(SellerPricingApiLinks.createPreference());
        model.add(SellerPricingApiLinks.getPreference(null));

        return ResponseEntity.ok(model);
    }
}
