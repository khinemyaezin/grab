package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.controller.ProductController;
import com.grab.store.catalog.internal.api.rest.dto.response.ProductSummaryResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProductSummaryModelAssembler
        implements RepresentationModelAssembler<ProductSummaryResponse, EntityModel<ProductSummaryResponse>> {

    @Override
    public EntityModel<ProductSummaryResponse> toModel(ProductSummaryResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(ProductController.class).getProducts(null)).withSelfRel(),
                linkTo(methodOn(ProductController.class).getVariationMatrix(null)).withRel("combination")
        );
    }
}
