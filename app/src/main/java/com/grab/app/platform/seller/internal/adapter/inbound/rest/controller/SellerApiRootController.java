package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/seller/v1")
public class SellerApiRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(linkTo(methodOn(SellerApiRootController.class).root()).withSelfRel());
        model.add(Link.of("/api/seller/v1/products", "get-products"));
        model.add(Link.of("/api/seller/v1/categories", "get-categories"));
        model.add(Link.of("/api/seller/v1/inventory/items", "get-inventory"));
        model.add(Link.of("/api/seller/v1/facilities/locations", "get-facilities"));
        model.add(Link.of("/api/seller/v1/pricing/price-lists", "get-pricing"));
        model.add(Link.of("/api/seller/v1/merchants", "get-merchants"));
        model.add(Link.of("/api/seller/v1/sales-channels", "get-sales-channels"));
        model.add(Link.of("/api/seller/v1/workflows/products/sellable", "get-sellable-products-workflow"));
        return ResponseEntity.ok(model);
    }
}

