package com.grab.app.platform.seller.internal.adapter.inbound.rest.region.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.RegionController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.region.rest.dto.response.RegionResponse;
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
