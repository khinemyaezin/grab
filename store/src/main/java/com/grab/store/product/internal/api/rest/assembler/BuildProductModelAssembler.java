package com.grab.store.product.internal.api.rest.assembler;

import com.grab.store.product.internal.api.rest.dto.response.BuildProductResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class BuildProductModelAssembler
        implements RepresentationModelAssembler<BuildProductResponse, EntityModel<BuildProductResponse>> {

    @Override
    public EntityModel<BuildProductResponse> toModel(BuildProductResponse response) {
        return EntityModel.of(response,
                Link.of("/api/v1/products/build").withSelfRel(),
                Link.of("/api/v1/products").withRel("products"),
                Link.of("/api/v1/products/combinations").withRel("combinations")
        );
    }
}
