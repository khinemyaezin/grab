package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.controller.ProductController;
import com.grab.store.catalog.internal.api.rest.dto.response.DeleteProductResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class DeleteProductModelAssembler
        implements RepresentationModelAssembler<DeleteProductResponse, EntityModel<DeleteProductResponse>> {

    @Override
    public EntityModel<DeleteProductResponse> toModel(DeleteProductResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(ProductController.class).getProducts(null)).withRel("products"),
                linkTo(methodOn(ProductController.class).saveProduct(null)).withRel("build"),
                linkTo(methodOn(ProductController.class).getVariationMatrix(null)).withRel("combinations")
        );
    }
}
