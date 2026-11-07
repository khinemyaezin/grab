package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerInventoryController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.InventoryItemResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class InventoryItemModelAssembler implements RepresentationModelAssembler<InventoryItemResponse, EntityModel<InventoryItemResponse>> {

    @Override
    public EntityModel<InventoryItemResponse> toModel(InventoryItemResponse item) {
        if (item == null) {
            return null;
        }
        EntityModel<InventoryItemResponse> model = EntityModel.of(item);
        if (item.id() != null) {
            model.add(linkTo(methodOn(SellerInventoryController.class).getItem(item.id())).withSelfRel());
            model.add(linkTo(methodOn(SellerInventoryController.class).getLatestMovement(item.id())).withRel("get-inventory-movement-latest"));
        }
        return model;
    }
}
