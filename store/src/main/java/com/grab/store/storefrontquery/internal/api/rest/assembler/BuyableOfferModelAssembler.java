package com.grab.store.storefrontquery.internal.api.rest.assembler;

import com.grab.store.storefrontquery.internal.api.rest.controller.StorefrontProductController;
import com.grab.store.storefrontquery.internal.api.rest.dto.response.BuyableOfferResponse;
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
        String salesChannelId = response.salesChannelId();

        Link selfLink = linkTo(methodOn(StorefrontProductController.class)
                .get(slug, salesChannelId))
                .withSelfRel();

        Link listLink = linkTo(methodOn(StorefrontProductController.class)
                .list(salesChannelId, null, null, null, null))
                .withRel("list-storefront-products");

        return EntityModel.of(response, selfLink, listLink);
    }
}
