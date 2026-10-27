package com.grab.app.platform.seller.internal.adapter.inbound.rest.discovery.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.StorefrontProductController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.discovery.rest.dto.response.BuyableOfferResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class BuyableOfferModelAssembler
        implements RepresentationModelAssembler<BuyableOfferResponse, EntityModel<BuyableOfferResponse>> {

    @Override
    public EntityModel<BuyableOfferResponse> toModel(BuyableOfferResponse response) {
        String slug = response.slug();
        Link selfLink = linkTo(methodOn(StorefrontProductController.class)
                .getBySlug(slug, null, response.countryCode(), response.locale()))
                .withSelfRel();
        Link listLink = linkTo(methodOn(StorefrontProductController.class)
                .list(null, response.countryCode(), response.locale(), null, null, null, null))
                .withRel("list-storefront-products");
        return EntityModel.of(response, selfLink, listLink);
    }
}
