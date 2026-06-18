package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.controller.ProductController;
import com.grab.store.catalog.internal.api.rest.dto.response.UpdateProductStatusResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UpdateProductStatusModelAssembler implements RepresentationModelAssembler<UpdateProductStatusResponse, EntityModel<UpdateProductStatusResponse>> {

    @Override
    public EntityModel<UpdateProductStatusResponse> toModel(UpdateProductStatusResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(ProductController.class).getProduct(response.productId())).withRel("get-product"),
                linkTo(methodOn(ProductController.class).getProducts(null, null, null)).withRel("search-products")
        );
    }
}
