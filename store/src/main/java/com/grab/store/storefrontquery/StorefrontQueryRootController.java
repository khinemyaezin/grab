package com.grab.store.storefrontquery;

import com.grab.store.storefrontquery.internal.api.rest.controller.StorefrontProductController;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/storefront-query")
public class StorefrontQueryRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(linkTo(methodOn(StorefrontQueryRootController.class).root()).withSelfRel());
        model.add(linkTo(methodOn(StorefrontProductController.class).list(null, null))
                .withRel("list-storefront-products"));
        model.add(linkTo(methodOn(StorefrontProductController.class).get(null, null))
                .withRel("get-storefront-product"));
        return ResponseEntity.ok(model);
    }
}
