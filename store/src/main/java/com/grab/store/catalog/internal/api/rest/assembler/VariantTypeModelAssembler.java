package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.controller.VariantTypeController;
import com.grab.store.catalog.internal.api.rest.dto.response.VariantTypeResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class VariantTypeModelAssembler implements RepresentationModelAssembler<VariantTypeResponse, EntityModel<VariantTypeResponse>> {

    public EntityModel<VariantTypeResponse> toModel(VariantTypeResponse response, String name) {
        return EntityModel.of(response,
                linkTo(methodOn(VariantTypeController.class).getVariantTypesByName(name)).withSelfRel()
        );
    }

    @Override
    public EntityModel<VariantTypeResponse> toModel(VariantTypeResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(VariantTypeController.class).getVariantTypesByName(null)).withRel("search-variant-types")
        );
    }
}
