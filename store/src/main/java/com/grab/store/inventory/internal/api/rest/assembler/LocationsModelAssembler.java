package com.grab.store.inventory.internal.api.rest.assembler;

import com.grab.store.inventory.internal.api.rest.dto.response.LocationsResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class LocationsModelAssembler
        implements RepresentationModelAssembler<LocationsResponse, EntityModel<LocationsResponse>> {

    @Override
    public EntityModel<LocationsResponse> toModel(LocationsResponse response) {
        return EntityModel.of(response,
                Link.of("/api/v1/locations").withSelfRel()
        );
    }
}
