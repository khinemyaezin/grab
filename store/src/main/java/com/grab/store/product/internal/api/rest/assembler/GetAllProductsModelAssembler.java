package com.grab.store.product.internal.api.rest.assembler;

import com.grab.store.product.internal.api.rest.dto.response.GetAllProductsResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class GetAllProductsModelAssembler
        implements RepresentationModelAssembler<GetAllProductsResponse, EntityModel<GetAllProductsResponse>> {

    @Override
    public EntityModel<GetAllProductsResponse> toModel(GetAllProductsResponse response) {
        return EntityModel.of(response,
                Link.of("/api/v1/products").withSelfRel(),
                Link.of("/api/v1/products/build").withRel("build"),
                Link.of("/api/v1/products/combinations").withRel("combinations")
        );
    }
}
