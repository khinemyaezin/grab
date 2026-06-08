package com.grab.store.inventory.internal.api.rest.assembler;

import com.grab.store.inventory.internal.api.rest.controller.InventoryController;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryResponse;
import org.springframework.hateoas.EntityModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class InventoryModelAssembler
        implements RepresentationModelAssembler<InventoryResponse, EntityModel<InventoryResponse>> {

    @Override
    public EntityModel<InventoryResponse> toModel(InventoryResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(InventoryController.class).getInventory(response.id())).withSelfRel()
        );
    }
}
