package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.controller.ProductController;
import com.grab.store.catalog.internal.api.rest.dto.response.GetProductBySlugResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class GetProductBySlugModelAssembler
        implements RepresentationModelAssembler<GetProductBySlugResponse, EntityModel<GetProductBySlugResponse>> {

    @Override
    public EntityModel<GetProductBySlugResponse> toModel(GetProductBySlugResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(ProductController.class).getProductBySlug(response.slug())).withSelfRel(),
                linkTo(methodOn(ProductController.class).getProduct(response.id())).withRel("product"),
                linkTo(methodOn(ProductController.class).getProducts(null)).withRel("products")
        );
    }
}
