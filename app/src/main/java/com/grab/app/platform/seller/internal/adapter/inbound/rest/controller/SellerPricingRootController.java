package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/pricing")
public class SellerPricingRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(linkTo(methodOn(SellerPricingRootController.class).root()).withSelfRel());

        model.add(linkTo(methodOn(SellerPriceListController.class).listPriceLists())
                .withRel("list-price-lists"));
        model.add(linkTo(methodOn(SellerPriceListController.class).createPriceList(null))
                .withRel("create-price-list"));

        model.add(linkTo(methodOn(SellerPriceSetController.class).createPriceSet())
                .withRel("create-price-set"));
        model.add(linkTo(methodOn(SellerPriceSetController.class).calculatePrices(null))
                .withRel("calculate-prices"));

        model.add(linkTo(methodOn(SellerPricePreferenceController.class).createPreference(null))
                .withRel("create-price-preference"));
        model.add(linkTo(methodOn(SellerPricePreferenceController.class).getPreference(null))
                .withRel("get-price-preference"));

        return ResponseEntity.ok(model);
    }
}
