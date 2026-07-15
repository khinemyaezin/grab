package com.grab.store.inventory.internal.api.rest.assembler;

import com.grab.store.inventory.internal.api.rest.controller.InventoryController;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class InventoryModelAssembler
        implements RepresentationModelAssembler<InventoryResponse, EntityModel<InventoryResponse>> {

    @Override
    public EntityModel<InventoryResponse> toModel(InventoryResponse response) {
        EntityModel<InventoryResponse> entity = EntityModel.of(response);

        entity.add(linkTo(methodOn(InventoryController.class)
                .getInventory(response.id()))
                .withSelfRel());
        entity.add(linkTo(methodOn(InventoryController.class)
                .receiveStock(response.id(), null, null))
                .withRel("receive"));
        entity.add(linkTo(methodOn(InventoryController.class)
                .adjustStock(response.id(), null, null))
                .withRel("adjust"));
        entity.add(linkTo(methodOn(InventoryController.class)
                .getMovements(response.id(), null, null))
                .withRel("movements"));
        entity.add(linkTo(methodOn(InventoryController.class)
                .getReservations(response.id(), null, null))
                .withRel("reservations"));

        return entity;
    }
}
