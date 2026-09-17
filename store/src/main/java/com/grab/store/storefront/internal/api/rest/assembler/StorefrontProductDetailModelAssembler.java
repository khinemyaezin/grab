package com.grab.store.storefront.internal.api.rest.assembler;

import com.grab.store.storefront.internal.api.rest.controller.StorefrontProductController;
import com.grab.store.storefront.internal.api.rest.dto.response.StorefrontProductDetail;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class StorefrontProductDetailModelAssembler
        implements RepresentationModelAssembler<StorefrontProductDetail, EntityModel<StorefrontProductDetail>> {

    @Override
    public EntityModel<StorefrontProductDetail> toModel(StorefrontProductDetail detail) {
        return EntityModel.of(detail,
                linkTo(methodOn(StorefrontProductController.class).bySlug(detail.slug())).withSelfRel(),
                linkTo(methodOn(StorefrontProductController.class).search(null, null, null)).withRel("search-products"));
    }
}
