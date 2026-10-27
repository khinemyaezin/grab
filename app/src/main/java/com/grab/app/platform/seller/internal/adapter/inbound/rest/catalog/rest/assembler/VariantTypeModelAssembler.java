package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.VariantTypeController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.VariantTypeResponse;
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
