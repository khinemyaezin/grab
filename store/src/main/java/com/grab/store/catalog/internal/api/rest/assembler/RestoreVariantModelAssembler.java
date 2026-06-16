package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.controller.ProductController;
import com.grab.store.catalog.internal.api.rest.dto.response.RestoreVariantResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class RestoreVariantModelAssembler implements RepresentationModelAssembler<RestoreVariantResponse, EntityModel<RestoreVariantResponse>> {

    @Override
    public EntityModel<RestoreVariantResponse> toModel(RestoreVariantResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(ProductController.class).getProduct(response.productId())).withSelfRel(),
                linkTo(methodOn(ProductController.class).getProducts(null)).withRel("products")
        );
    }
}
