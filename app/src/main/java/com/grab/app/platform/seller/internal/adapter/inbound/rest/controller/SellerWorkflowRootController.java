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
@RequestMapping("/api/v1/workflows/root")
public class SellerWorkflowRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(linkTo(methodOn(SellerWorkflowRootController.class).root()).withSelfRel());

        model.add(linkTo(methodOn(SellerSellableProductWorkflowController.class).startCreateSellableProduct(null, null))
                .withRel("create-sellable-product"));
        model.add(linkTo(methodOn(SellerSellableProductWorkflowController.class).getCreateSellableProduct(null))
                .withRel("get-create-sellable-product"));

        model.add(linkTo(methodOn(SellerSellableProductWorkflowController.class).startUpdateSellableProduct(null, null))
                .withRel("update-sellable-product"));
        model.add(linkTo(methodOn(SellerSellableProductWorkflowController.class).getUpdateSellableProduct(null))
                .withRel("get-update-sellable-product"));

        model.add(linkTo(methodOn(SellerSellableProductWorkflowController.class).startUpdateProductVariant(null, null))
                .withRel("update-product-variant"));
        model.add(linkTo(methodOn(SellerSellableProductWorkflowController.class).getUpdateProductVariant(null))
                .withRel("get-update-product-variant"));

        return ResponseEntity.ok(model);
    }
}

