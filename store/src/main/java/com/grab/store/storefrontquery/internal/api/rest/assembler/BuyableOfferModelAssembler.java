package com.grab.store.storefrontquery.internal.api.rest.assembler;

import com.grab.store.storefrontquery.internal.api.rest.controller.StorefrontProductController;
import com.grab.store.storefrontquery.internal.api.rest.dto.response.BuyableOfferResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class BuyableOfferModelAssembler
        implements RepresentationModelAssembler<BuyableOfferResponse, EntityModel<BuyableOfferResponse>> {
    @Override
    public EntityModel<BuyableOfferResponse> toModel(BuyableOfferResponse response) {
        return EntityModel.of(
                response,
                linkTo(methodOn(StorefrontProductController.class)
                        .get(response.slug(), response.salesChannelId()))
                        .withSelfRel(),
                linkTo(methodOn(StorefrontProductController.class)
                        .list(response.salesChannelId(), null))
                        .withRel("list-storefront-products")
        );
    }
}
