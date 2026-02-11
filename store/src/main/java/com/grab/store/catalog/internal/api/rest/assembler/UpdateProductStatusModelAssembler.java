package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.dto.response.UpdateProductStatusResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class UpdateProductStatusModelAssembler implements RepresentationModelAssembler<UpdateProductStatusResponse, EntityModel<UpdateProductStatusResponse>> {

    @Override
    public EntityModel<UpdateProductStatusResponse> toModel(UpdateProductStatusResponse response) {
        return EntityModel.of(response,
                Link.of("/api/v1/products/" + response.productId()).withSelfRel(),
                Link.of("/api/v1/products").withRel("products")
        );
    }
}
