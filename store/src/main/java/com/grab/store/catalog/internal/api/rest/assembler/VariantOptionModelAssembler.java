package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.dto.response.VariantOptionResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class VariantOptionModelAssembler implements RepresentationModelAssembler<VariantOptionResponse, EntityModel<VariantOptionResponse>> {

    @Override
    public EntityModel<VariantOptionResponse> toModel(VariantOptionResponse response) {
        String name = response.options().isEmpty() ? "" : response.options().get(0).name();
        return EntityModel.of(response,
                Link.of("/api/v1/variant-options?name=" + name).withSelfRel()
        );
    }
}
