package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.assembler;

import com.grab.app.catalog.catalog.api.CatalogApiLinks;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.InventoryController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.CheckInventoryExistenceResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CheckInventoryExistenceModelAssembler
        implements RepresentationModelAssembler<CheckInventoryExistenceResponse, EntityModel<CheckInventoryExistenceResponse>> {

    @Override
    public EntityModel<CheckInventoryExistenceResponse> toModel(CheckInventoryExistenceResponse response) {
        EntityModel<CheckInventoryExistenceResponse> entity = EntityModel.of(response);

        entity.add(linkTo(methodOn(InventoryController.class)
                .checkExistence(null, null))
                .withRel("check-inventory-items-existence"));
        entity.add(linkTo(methodOn(InventoryController.class)
                .createInventory(null, null))
                .withRel("create-inventory-item"));
        entity.add(linkTo(methodOn(InventoryController.class)
                .searchInventoryItems(null, null, null, null))
                .withRel("search-inventory-items"));
        entity.add(CatalogApiLinks.searchProductVariants());

        return entity;
    }
}
