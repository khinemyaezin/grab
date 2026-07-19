package com.grab.store.inventory.internal.api.rest.assembler;

import com.grab.store.inventory.internal.api.rest.controller.InventoryController;
import com.grab.store.inventory.internal.api.rest.controller.ReorderSuggestionController;
import com.grab.store.inventory.internal.api.rest.dto.response.ReorderSuggestionResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ReorderSuggestionModelAssembler {

    public CollectionModel<EntityModel<ReorderSuggestionResponse>> toCollectionModel(
            List<ReorderSuggestionResponse> responses,
            String locationId,
            String sku
    ) {
        List<EntityModel<ReorderSuggestionResponse>> models = responses.stream()
                .map(this::toModel)
                .toList();
        CollectionModel<EntityModel<ReorderSuggestionResponse>> collection = CollectionModel.of(models);
        collection.add(linkTo(methodOn(ReorderSuggestionController.class).list(locationId, sku, null)).withSelfRel());
        return collection;
    }

    public EntityModel<ReorderSuggestionResponse> toModel(ReorderSuggestionResponse response) {
        EntityModel<ReorderSuggestionResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(InventoryController.class).getInventory(response.inventoryItemId())).withRel("inventory-item"));
        return model;
    }
}
