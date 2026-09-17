package com.grab.store.storefront.internal.api.rest.assembler;

import com.grab.store.storefront.internal.api.rest.controller.StorefrontProductController;
import com.grab.store.storefront.internal.api.rest.dto.response.StorefrontProductCard;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class StorefrontProductCardModelAssembler
        implements RepresentationModelAssembler<StorefrontProductCard, EntityModel<StorefrontProductCard>> {

    @Override
    public EntityModel<StorefrontProductCard> toModel(StorefrontProductCard card) {
        return EntityModel.of(card,
                linkTo(methodOn(StorefrontProductController.class).bySlug(card.slug())).withSelfRel());
    }
}
