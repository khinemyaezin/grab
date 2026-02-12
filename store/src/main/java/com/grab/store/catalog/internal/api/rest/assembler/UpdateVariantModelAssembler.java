package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.dto.response.UpdateVariantResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class UpdateVariantModelAssembler implements RepresentationModelAssembler<UpdateVariantResponse, EntityModel<UpdateVariantResponse>> {

    @Override
    public EntityModel<UpdateVariantResponse> toModel(UpdateVariantResponse response) {
        return EntityModel.of(response,
                Link.of("/api/v1/products/" + response.productId() + "/variants/" + response.variantId()).withSelfRel(),
                Link.of("/api/v1/products/" + response.productId()).withRel("product")
        );
    }
}
