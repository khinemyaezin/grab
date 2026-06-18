package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.controller.VariantOptionController;
import com.grab.store.catalog.internal.api.rest.dto.response.VariantOptionResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class VariantOptionModelAssembler implements RepresentationModelAssembler<VariantOptionResponse, EntityModel<VariantOptionResponse>> {

    public EntityModel<VariantOptionResponse> toModel(VariantOptionResponse response, String name, String typeId) {
        return EntityModel.of(response,
                linkTo(methodOn(VariantOptionController.class).getVariantOptionsByName(name, typeId)).withSelfRel()
        );
    }

    @Override
    public EntityModel<VariantOptionResponse> toModel(VariantOptionResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(VariantOptionController.class).getVariantOptionsByName(null, null)).withRel("search-variant-options")
        );
    }
}
