package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateSellableProductResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerWorkflowApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class UpdateSellableProductModelAssembler
        implements RepresentationModelAssembler<UpdateSellableProductResponse, EntityModel<UpdateSellableProductResponse>> {

    @Override
    public EntityModel<UpdateSellableProductResponse> toModel(UpdateSellableProductResponse response) {
        return assemble(response, true);
    }

    public EntityModel<UpdateSellableProductResponse> toCommandModel(UpdateSellableProductResponse response) {
        return assemble(response, false);
    }

    private EntityModel<UpdateSellableProductResponse> assemble(UpdateSellableProductResponse response, boolean canonicalRead) {
        if (response == null) {
            return null;
        }
        EntityModel<UpdateSellableProductResponse> model = EntityModel.of(response, SellerWorkflowApiLinks.startUpdateSellableProduct());
        if (response.workflowId() != null) {
            Link retrieval = SellerWorkflowApiLinks.getUpdateSellableProduct(response.workflowId());
            model.add(canonicalRead ? retrieval.withSelfRel() : retrieval);
        }
        return model;
    }
}
