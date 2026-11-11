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
@RequestMapping("/api/v1/merchants/root")
public class SellerMerchantRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(linkTo(methodOn(SellerMerchantRootController.class).root()).withSelfRel());

        model.add(linkTo(methodOn(SellerMerchantController.class).listMyMerchants(null))
                .withRel("list-merchants"));
        model.add(linkTo(methodOn(SellerMerchantController.class).getMerchant(null, null))
                .withRel("get-merchant"));
        model.add(linkTo(methodOn(SellerMerchantController.class).startApplication(null, null))
                .withRel("create-merchant-application"));

        model.add(linkTo(methodOn(SellerStorefrontController.class).listStorefronts(null))
                .withRel("list-storefronts"));
        model.add(linkTo(methodOn(SellerStorefrontController.class).createStorefront(null, null))
                .withRel("create-storefront"));

        return ResponseEntity.ok(model);
    }
}

