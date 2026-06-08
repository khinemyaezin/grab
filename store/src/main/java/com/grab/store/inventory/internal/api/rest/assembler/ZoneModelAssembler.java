package com.grab.store.inventory.internal.api.rest.assembler;

import com.grab.store.inventory.internal.api.rest.controller.ZoneController;
import com.grab.store.inventory.internal.api.rest.dto.response.ZoneResponse;
import com.grab.store.shared.LinkRelations;
import org.springframework.hateoas.EntityModel;
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
                .updateZone(response.id(), null, null)).withRel(LinkRelations.UPDATE));

        entity.add(linkTo(methodOn(ZoneController.class)
                .listZones(response.locationId(), null, null)).withRel(LinkRelations.ZONES));

        if (response.active()) {
            entity.add(linkTo(methodOn(ZoneController.class)
                    .deactivateZone(response.id(), null)).withRel(LinkRelations.DEACTIVATE));
        } else {
            entity.add(linkTo(methodOn(ZoneController.class)
                    .activateZone(response.id(), null)).withRel(LinkRelations.ACTIVATE));
        }

        return entity;
    }
}
