package com.grab.app.platform.seller.internal.adapter.inbound.rest.discovery.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.StorefrontProductController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.discovery.rest.dto.response.ShoppingContextResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ShoppingContextModelAssembler
        implements RepresentationModelAssembler<ShoppingContextResponse, EntityModel<ShoppingContextResponse>> {
    @Override
    public EntityModel<ShoppingContextResponse> toModel(ShoppingContextResponse response) {
        return toModel(response, response.countryCode(), response.locale());
    }

    public EntityModel<ShoppingContextResponse> toModel(
            ShoppingContextResponse response,
            String countryCode,
            String locale
    ) {
        Link selfLink = linkTo(methodOn(StorefrontProductController.class)
                .resolveContext(null, countryCode, locale))
                .withSelfRel();
        return EntityModel.of(response, selfLink);
    }
}
