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
@RequestMapping("/api/v1/auth")
public class SellerAuthRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(linkTo(methodOn(SellerAuthRootController.class).root()).withSelfRel());

        model.add(linkTo(methodOn(SellerAuthController.class).login(null))
                .withRel("login"));
        model.add(linkTo(methodOn(SellerAuthController.class).logout(null, null))
                .withRel("logout"));
        model.add(linkTo(methodOn(SellerAuthController.class).refresh(null, null))
                .withRel("refresh"));
        model.add(linkTo(methodOn(SellerAuthController.class).register(null))
                .withRel("register"));

        return ResponseEntity.ok(model);
    }
}

