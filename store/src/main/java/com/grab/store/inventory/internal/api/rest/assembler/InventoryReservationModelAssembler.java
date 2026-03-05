package com.grab.store.inventory.internal.api.rest.assembler;

import com.grab.store.inventory.internal.api.rest.dto.response.InventoryReservationResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class InventoryReservationModelAssembler
        implements RepresentationModelAssembler<InventoryReservationResponse, EntityModel<InventoryReservationResponse>> {

    @Override
    public EntityModel<InventoryReservationResponse> toModel(InventoryReservationResponse response) {
        return EntityModel.of(response,
                Link.of("/api/v1/inventories/" + response.inventoryItemId() + "/reservations/" + response.id()).withSelfRel(),
                Link.of("/api/v1/inventories/" + response.inventoryItemId()).withRel("inventory")
        );
    }
}
