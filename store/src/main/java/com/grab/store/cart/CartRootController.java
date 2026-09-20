package com.grab.store.cart;

import com.grab.store.cart.internal.api.rest.controller.CartController;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/carts")
public class CartRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(linkTo(methodOn(CartRootController.class).root()).withSelfRel());
        model.add(linkTo(methodOn(CartController.class).addItem(null, null))
                .withRel("add-cart-item"));
        model.add(linkTo(methodOn(CartController.class).current(null, null, null))
                .withRel("get-current-cart"));
        return ResponseEntity.ok(model);
    }
}
