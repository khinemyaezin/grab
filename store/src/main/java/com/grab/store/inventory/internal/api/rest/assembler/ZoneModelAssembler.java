package com.grab.store.inventory.internal.api.rest.assembler;

import com.grab.store.inventory.internal.api.rest.controller.BinController;
import com.grab.store.inventory.internal.api.rest.controller.ZoneController;
import com.grab.store.inventory.internal.api.rest.dto.response.ZoneResponse;
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
                .getZoneById(response.id()))
                .withSelfRel());

        entity.add(linkTo(methodOn(ZoneController.class)
                .updateZone(response.id(), null, null))
                .withRel("edit-zone"));

        if (response.active()) {
            entity.add(linkTo(methodOn(ZoneController.class)
                    .deactivateZone(response.id(), null))
                    .withRel("deactivate-zone"));
        } else {
            entity.add(linkTo(methodOn(ZoneController.class)
                    .activateZone(response.id(), null))
                    .withRel("activate-zone"));
        }

        entity.add(linkTo(methodOn(BinController.class)
                .listBins(response.id(), null, null, null))
                .withRel("paged-bin"));

        return entity;
    }
}
