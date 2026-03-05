package com.grab.store.inventory.internal.api.rest.assembler;

import com.grab.store.inventory.internal.api.rest.dto.response.InventoryMovementsResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class InventoryMovementsModelAssembler
        implements RepresentationModelAssembler<InventoryMovementsResponse, EntityModel<InventoryMovementsResponse>> {

    @Override
    public EntityModel<InventoryMovementsResponse> toModel(InventoryMovementsResponse response) {
        return EntityModel.of(response,
                Link.of("/api/v1/inventories/" + response.inventoryItemId() + "/movements").withSelfRel(),
                Link.of("/api/v1/inventories/" + response.inventoryItemId()).withRel("inventory")
        );
    }
}
