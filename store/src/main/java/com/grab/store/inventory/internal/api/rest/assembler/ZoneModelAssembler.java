package com.grab.store.inventory.internal.api.rest.assembler;

import com.grab.store.inventory.internal.api.rest.controller.ZoneController;
import com.grab.store.inventory.internal.api.rest.dto.response.ZoneResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ZoneModelAssembler implements RepresentationModelAssembler<ZoneResponse, EntityModel<ZoneResponse>> {

    @Override
    public EntityModel<ZoneResponse> toModel(ZoneResponse response) {
        EntityModel<ZoneResponse> entity = EntityModel.of(response);

        entity.add(linkTo(methodOn(ZoneController.class)
                .getZoneById(response.id())).withSelfRel());

        entity.add(linkTo(methodOn(ZoneController.class)
                .createZone(response.id(), null, null)).withRel("create"));

        entity.add(linkTo(methodOn(ZoneController.class)
                .updateZone(response.id(), null, null)).withRel("update"));

        entity.add(linkTo(methodOn(ZoneController.class)
                .listZones(response.locationId(), null, null)).withRel("zones"));

        if (response.active()) {
            entity.add(linkTo(methodOn(ZoneController.class)
                    .deactivateZone(response.id(), null)).withRel("deactivate"));
        } else {
            entity.add(linkTo(methodOn(ZoneController.class)
                    .activateZone(response.id(), null)).withRel("activate"));
        }

        return entity;
    }
}
