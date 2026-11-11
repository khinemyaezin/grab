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
@RequestMapping("/api/v1")
public class SellerApiRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(linkTo(methodOn(SellerApiRootController.class).root()).withSelfRel());

        model.add(linkTo(methodOn(SellerCatalogRootController.class).root()).withRel("get-catalog-root"));
        model.add(linkTo(methodOn(SellerInventoryRootController.class).root()).withRel("get-inventory-root"));
        model.add(linkTo(methodOn(SellerMerchantRootController.class).root()).withRel("get-merchant-root"));
        model.add(linkTo(methodOn(SellerPricingRootController.class).root()).withRel("get-pricing-root"));
        model.add(linkTo(methodOn(SellerSalesChannelRootController.class).root()).withRel("get-sales-channel-root"));
        model.add(linkTo(methodOn(SellerWorkflowRootController.class).root()).withRel("get-workflows-root"));
        model.add(linkTo(methodOn(SellerAuthRootController.class).root()).withRel("get-identity-root"));

        return ResponseEntity.ok(model);
    }
}
