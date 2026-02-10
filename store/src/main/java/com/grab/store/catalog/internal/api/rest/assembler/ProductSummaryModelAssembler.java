package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.dto.response.ProductSummaryResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class ProductSummaryModelAssembler
        implements RepresentationModelAssembler<ProductSummaryResponse, EntityModel<ProductSummaryResponse>> {

    @Override
    public EntityModel<ProductSummaryResponse> toModel(ProductSummaryResponse response) {
        return EntityModel.of(response,
                Link.of("/api/v1/products").withSelfRel(),
                Link.of("/api/v1/products/combination").withRel("combination")
        );
    }
}