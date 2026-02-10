package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.dto.response.ProductCombinationResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class ProductCombinationModelAssembler
        implements RepresentationModelAssembler<ProductCombinationResponse, EntityModel<ProductCombinationResponse>> {

    @Override
    public EntityModel<ProductCombinationResponse> toModel(ProductCombinationResponse response) {
        return EntityModel.of(response,
                Link.of("/api/v1/products/combination").withSelfRel(),
                Link.of("/api/v1/products").withRel("products")
        );
    }
}
