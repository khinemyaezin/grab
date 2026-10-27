package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.ProductController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.UpdateProductStatusResponse;
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
                linkTo(methodOn(ProductController.class).searchProducts(null, null, null)).withRel("search-products")
        );
    }
}
