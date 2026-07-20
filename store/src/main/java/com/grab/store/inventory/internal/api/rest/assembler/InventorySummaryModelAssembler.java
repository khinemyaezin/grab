package com.grab.store.inventory.internal.api.rest.assembler;

import com.grab.store.inventory.internal.api.rest.controller.InventoryController;
import com.grab.store.inventory.internal.api.rest.dto.response.InventorySummaryResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class InventorySummaryModelAssembler
        implements RepresentationModelAssembler<InventorySummaryResponse, EntityModel<InventorySummaryResponse>> {

    @Override
    public EntityModel<InventorySummaryResponse> toModel(InventorySummaryResponse response) {
        EntityModel<InventorySummaryResponse> entity = EntityModel.of(response);
        entity.add(linkTo(methodOn(InventoryController.class)
                .getInventorySummary(null, null))
                .withSelfRel());
        entity.add(linkTo(methodOn(InventoryController.class)
                .searchInventoryItems(null, null, null, null))
                .withRel("search-inventory-items"));
        return entity;
    }
}
