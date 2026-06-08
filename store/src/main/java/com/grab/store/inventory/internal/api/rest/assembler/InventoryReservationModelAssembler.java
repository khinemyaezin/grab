package com.grab.store.inventory.internal.api.rest.assembler;

import com.grab.store.inventory.internal.api.rest.controller.InventoryController;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryReservationResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class InventoryReservationModelAssembler
        implements RepresentationModelAssembler<InventoryReservationResponse, EntityModel<InventoryReservationResponse>> {

    @Override
    public EntityModel<InventoryReservationResponse> toModel(InventoryReservationResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(InventoryController.class).getReservations(response.inventoryItemId(),null, null)).withRel("reservations")
        );
    }
}
