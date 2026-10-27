package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.ProductController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.RestoreVariantResponse;
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
                linkTo(methodOn(ProductController.class)
                        .getVariant(response.productId(), response.variantId())).withRel("get-variant"),
                linkTo(methodOn(ProductController.class).getProduct(response.productId())).withRel("get-product"),
                linkTo(methodOn(ProductController.class).searchProducts(null, null, null)).withRel("search-products")
        );
    }
}
