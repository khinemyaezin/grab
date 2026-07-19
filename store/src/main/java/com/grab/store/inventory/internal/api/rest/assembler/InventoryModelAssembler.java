package com.grab.store.inventory.internal.api.rest.assembler;

import com.grab.store.inventory.internal.api.rest.controller.InventoryController;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryResponse;
import com.inventory.domain.enums.InventoryStatus;
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
                .getMovements(response.id(), null, null))
                .withRel("list-inventory-item-movements"));
        entity.add(linkTo(methodOn(InventoryController.class)
                .getReservations(response.id(), null, null))
                .withRel("list-inventory-item-reservations"));

        try {
            InventoryStatus status = InventoryStatus.valueOf(response.status().toUpperCase());
            if (status == InventoryStatus.ACTIVE || status == InventoryStatus.OUT_OF_STOCK) {
                addStockOperationLinks(entity, response);
                addLifecycleLinksForOperationalItem(entity, response);
                addReorderConfigLink(entity, response);
            } else if (status == InventoryStatus.SUSPENDED) {
                addLifecycleLinksForSuspendedItem(entity, response);
                addReorderConfigLink(entity, response);
            }
        } catch (IllegalArgumentException | NullPointerException ignored) {
        }

        return entity;
    }

    private void addStockOperationLinks(EntityModel<InventoryResponse> entity, InventoryResponse response) {
        entity.add(linkTo(methodOn(InventoryController.class)
                .receiveStock(response.id(), null, null))
                .withRel("receive-inventory-item"));
        entity.add(linkTo(methodOn(InventoryController.class)
                .reserveStock(response.id(), null, null, null))
                .withRel("reserve-inventory-item"));
        entity.add(linkTo(methodOn(InventoryController.class)
                .adjustStock(response.id(), null, null))
                .withRel("adjust-inventory-item"));
        entity.add(linkTo(methodOn(InventoryController.class)
                .markDamaged(response.id(), null, null))
                .withRel("damage-inventory-item"));
        entity.add(linkTo(methodOn(InventoryController.class)
                .writeOff(response.id(), null, null))
                .withRel("write-off-inventory-item"));
        entity.add(linkTo(methodOn(InventoryController.class)
                .returnToVendor(response.id(), null, null))
                .withRel("return-inventory-item-to-vendor"));
        entity.add(linkTo(methodOn(InventoryController.class)
                .transfer(response.id(), null, null))
                .withRel("transfer-inventory-item"));
        entity.add(linkTo(methodOn(InventoryController.class)
                .announceInTransit(response.id(), null, null))
                .withRel("announce-in-transit"));
        entity.add(linkTo(methodOn(InventoryController.class)
                .receiveInTransit(response.id(), null, null))
                .withRel("receive-in-transit"));
    }

    private void addLifecycleLinksForOperationalItem(
            EntityModel<InventoryResponse> entity, InventoryResponse response) {
        entity.add(linkTo(methodOn(InventoryController.class)
                .suspend(response.id(), null))
                .withRel("suspend-inventory-item"));
        entity.add(linkTo(methodOn(InventoryController.class)
                .discontinue(response.id(), null))
                .withRel("discontinue-inventory-item"));
    }

    private void addLifecycleLinksForSuspendedItem(
            EntityModel<InventoryResponse> entity, InventoryResponse response) {
        entity.add(linkTo(methodOn(InventoryController.class)
                .activate(response.id(), null))
                .withRel("activate-inventory-item"));
        entity.add(linkTo(methodOn(InventoryController.class)
                .discontinue(response.id(), null))
                .withRel("discontinue-inventory-item"));
    }

    private void addReorderConfigLink(EntityModel<InventoryResponse> entity, InventoryResponse response) {
        entity.add(linkTo(methodOn(InventoryController.class)
                .updateReorderConfig(response.id(), null, null))
                .withRel("update-reorder-config"));
    }
}
