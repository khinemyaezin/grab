package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.InventoryItemResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerInventoryApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class InventoryItemModelAssembler implements RepresentationModelAssembler<InventoryItemResponse, EntityModel<InventoryItemResponse>> {

    @Override
    public EntityModel<InventoryItemResponse> toModel(InventoryItemResponse item) {
        return assemble(item, true);
    }

    public EntityModel<InventoryItemResponse> toCommandModel(InventoryItemResponse item) {
        return assemble(item, false);
    }

    private EntityModel<InventoryItemResponse> assemble(InventoryItemResponse item, boolean canonicalRead) {
        if (item == null) {
            return null;
        }
        EntityModel<InventoryItemResponse> model = EntityModel.of(item);
        if (item.id() != null) {
            Link retrieval = SellerInventoryApiLinks.getItem(item.id());
            model.add(canonicalRead ? retrieval.withSelfRel() : retrieval);
            model.add(SellerInventoryApiLinks.getLatestMovement(item.id()));
        }
        return model;
    }
}
