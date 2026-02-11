package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.dto.response.GetProductBySlugResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class GetProductBySlugModelAssembler
        implements RepresentationModelAssembler<GetProductBySlugResponse, EntityModel<GetProductBySlugResponse>> {

    @Override
    public EntityModel<GetProductBySlugResponse> toModel(GetProductBySlugResponse response) {
        return EntityModel.of(response,
                Link.of("/api/v1/products/slug/" + response.slug()).withSelfRel(),
                Link.of("/api/v1/products/" + response.id()).withRel("product"),
                Link.of("/api/v1/products").withRel("products")
        );
    }
}
