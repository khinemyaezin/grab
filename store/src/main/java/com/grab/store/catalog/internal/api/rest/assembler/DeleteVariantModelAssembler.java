package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.controller.ProductController;
import com.grab.store.catalog.internal.api.rest.dto.response.DeleteVariantResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class DeleteVariantModelAssembler implements RepresentationModelAssembler<DeleteVariantResponse, EntityModel<DeleteVariantResponse>> {

    @Override
    public EntityModel<DeleteVariantResponse> toModel(DeleteVariantResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(ProductController.class).getProduct(response.productId())).withSelfRel(),
                linkTo(methodOn(ProductController.class).getProducts(null)).withRel("products")
        );
    }
}
