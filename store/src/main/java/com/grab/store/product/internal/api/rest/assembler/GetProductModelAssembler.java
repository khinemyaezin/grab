package com.grab.store.product.internal.api.rest.assembler;

import com.grab.store.product.internal.api.rest.dto.response.GetProductResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class GetProductModelAssembler
        implements RepresentationModelAssembler<GetProductResponse, EntityModel<GetProductResponse>> {

    @Override
    public EntityModel<GetProductResponse> toModel(GetProductResponse response) {
        return EntityModel.of(response,
                Link.of("/api/v1/products/" + response.id()).withSelfRel(),
                Link.of("/api/v1/products").withRel("products")
        );
    }
}
