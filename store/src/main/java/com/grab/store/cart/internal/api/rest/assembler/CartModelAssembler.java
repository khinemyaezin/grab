package com.grab.store.cart.internal.api.rest.assembler;

import com.grab.store.cart.internal.api.rest.controller.CartController;
import com.grab.store.cart.internal.api.rest.dto.response.CartResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CartModelAssembler
        implements RepresentationModelAssembler<CartResponse, EntityModel<CartResponse>> {
    @Override
    public EntityModel<CartResponse> toModel(CartResponse response) {
        return EntityModel.of(
                response,
                linkTo(methodOn(CartController.class)
                        .current(response.guestToken(), response.salesChannelId(), response.regionId()))
                        .withSelfRel(),
                linkTo(methodOn(CartController.class).addItem(null, null))
                        .withRel("add-cart-item"),
                linkTo(methodOn(CartController.class)
                        .current(response.guestToken(), response.salesChannelId(), response.regionId()))
                        .withRel("get-current-cart")
        );
    }
}
