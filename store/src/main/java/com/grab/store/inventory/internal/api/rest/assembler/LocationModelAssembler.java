package com.grab.store.inventory.internal.api.rest.assembler;

import com.grab.store.inventory.internal.api.rest.dto.response.LocationResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class LocationModelAssembler
        implements RepresentationModelAssembler<LocationResponse, EntityModel<LocationResponse>> {

    @Override
    public EntityModel<LocationResponse> toModel(LocationResponse response) {
        return EntityModel.of(response,
                Link.of("/api/v1/locations/" + response.id()).withSelfRel(),
                Link.of("/api/v1/locations").withRel("locations")
        );
    }
}
