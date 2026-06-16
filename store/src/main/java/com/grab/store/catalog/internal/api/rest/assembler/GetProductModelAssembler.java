package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.controller.ProductController;
import com.grab.store.catalog.internal.api.rest.dto.response.GetProductResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class GetProductModelAssembler
        implements RepresentationModelAssembler<GetProductResponse, EntityModel<GetProductResponse>> {

    @Override
    public EntityModel<GetProductResponse> toModel(GetProductResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(ProductController.class).getProduct(response.id())).withSelfRel(),
                linkTo(methodOn(ProductController.class).getProducts(null)).withRel("products")
        );
    }
}
