package com.grab.store.inventory.internal.api.rest.assembler;

import com.grab.store.inventory.internal.api.rest.controller.LocationController;
import com.grab.store.inventory.internal.api.rest.controller.ZoneController;
import com.grab.store.inventory.internal.api.rest.dto.response.LocationResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class LocationModelAssembler
        implements RepresentationModelAssembler<LocationResponse, EntityModel<LocationResponse>> {

    @Override
    public EntityModel<LocationResponse> toModel(LocationResponse response) {
        EntityModel<LocationResponse> entity = EntityModel.of(response);

        entity.add(linkTo(methodOn(LocationController.class)
                .getLocation(response.id())).withSelfRel());

        entity.add(linkTo(methodOn(LocationController.class)
                .createLocation(null, null)).withRel("create"));

        entity.add(linkTo(methodOn(LocationController.class)
                .updateLocation(response.id(), null, null)).withRel("update"));

        entity.add(linkTo(methodOn(ZoneController.class)
                .listZones(response.id(), null, null)).withRel("zones"));

        if (response.active()) {
            entity.add(linkTo(methodOn(LocationController.class)
                    .deactivateLocation(response.id(), null)).withRel("deactivate"));
        } else {
            entity.add(linkTo(methodOn(LocationController.class)
                    .activateLocation(response.id(), null)).withRel("activate"));
        }

        return entity;
    }
}
