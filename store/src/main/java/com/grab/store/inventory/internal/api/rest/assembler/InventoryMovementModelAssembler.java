package com.grab.store.inventory.internal.api.rest.assembler;

import com.grab.store.inventory.internal.api.rest.controller.InventoryController;
import com.grab.store.inventory.internal.api.rest.dto.response.StockMovementResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class InventoryMovementModelAssembler
        implements RepresentationModelAssembler<StockMovementResponse, EntityModel<StockMovementResponse>> {

    @Override
    public EntityModel<StockMovementResponse> toModel(StockMovementResponse movement) {
        return EntityModel.of(movement,
                linkTo(methodOn(InventoryController.class)
                        .getMovements(movement.inventoryItemId(), null, null)).withSelfRel(),
                linkTo(methodOn(InventoryController.class)
                        .getMovements(movement.inventoryItemId(), null, null)).withRel("movements")
        );
    }
}