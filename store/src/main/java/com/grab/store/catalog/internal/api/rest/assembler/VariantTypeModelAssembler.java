package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.dto.response.VariantTypeResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class VariantTypeModelAssembler implements RepresentationModelAssembler<VariantTypeResponse, EntityModel<VariantTypeResponse>> {

    @Override
    public EntityModel<VariantTypeResponse> toModel(VariantTypeResponse response) {
        String name = response.types().isEmpty() ? "" : response.types().get(0).name();
        return EntityModel.of(response,
                Link.of("/api/v1/variant-types?name=" + name).withSelfRel()
        );
    }
}
