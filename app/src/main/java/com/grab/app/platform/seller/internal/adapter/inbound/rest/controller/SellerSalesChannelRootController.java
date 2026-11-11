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
@RequestMapping("/api/v1/sales-channels/root")
public class SellerSalesChannelRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(linkTo(methodOn(SellerSalesChannelRootController.class).root()).withSelfRel());

        model.add(linkTo(methodOn(SellerSalesChannelController.class).listChannels(null, 0, 20))
                .withRel("list-sales-channels"));
        model.add(linkTo(methodOn(SellerSalesChannelController.class).getChannel(null, null))
                .withRel("get-sales-channel"));
        model.add(linkTo(methodOn(SellerSalesChannelController.class).provisionMerchantWebsiteChannel(null, null))
                .withRel("create-merchant-website-channel"));

        model.add(linkTo(methodOn(SellerPublishableKeyController.class).resolveKey(null))
                .withRel("resolve-publishable-key"));

        return ResponseEntity.ok(model);
    }
}
