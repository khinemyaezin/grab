package com.grab.store.region.internal.api.rest.assembler;

import com.grab.store.region.internal.api.rest.controller.RegionController;
import com.grab.store.region.internal.api.rest.dto.response.RegionResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class RegionModelAssembler implements RepresentationModelAssembler<RegionResponse, EntityModel<RegionResponse>> {

    @Override
    public EntityModel<RegionResponse> toModel(RegionResponse response) {
        return EntityModel.of(
                response,
                linkTo(methodOn(RegionController.class).get(response.regionId())).withSelfRel(),
                linkTo(methodOn(RegionController.class).get(response.regionId())).withRel("get-region")
        );
    }
}
