package com.grab.store.inventory.internal.api.rest.assembler;

import com.grab.store.inventory.internal.api.rest.dto.response.InventoryReservationsResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class InventoryReservationsModelAssembler
        implements RepresentationModelAssembler<InventoryReservationsResponse, EntityModel<InventoryReservationsResponse>> {

    @Override
    public EntityModel<InventoryReservationsResponse> toModel(InventoryReservationsResponse response) {
        return EntityModel.of(response,
                Link.of("/api/v1/inventories/" + response.inventoryItemId() + "/reservations").withSelfRel(),
                Link.of("/api/v1/inventories/" + response.inventoryItemId()).withRel("inventory")
        );
    }
}
