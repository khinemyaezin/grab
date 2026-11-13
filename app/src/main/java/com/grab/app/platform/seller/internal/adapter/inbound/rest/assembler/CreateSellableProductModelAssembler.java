package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CreateSellableProductResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerWorkflowApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class CreateSellableProductModelAssembler
        implements RepresentationModelAssembler<CreateSellableProductResponse, EntityModel<CreateSellableProductResponse>> {

    @Override
    public EntityModel<CreateSellableProductResponse> toModel(CreateSellableProductResponse response) {
        return assemble(response, true);
    }

    public EntityModel<CreateSellableProductResponse> toCommandModel(CreateSellableProductResponse response) {
        return assemble(response, false);
    }

    private EntityModel<CreateSellableProductResponse> assemble(CreateSellableProductResponse response, boolean canonicalRead) {
        if (response == null) {
            return null;
        }
        EntityModel<CreateSellableProductResponse> model = EntityModel.of(response, SellerWorkflowApiLinks.startCreateSellableProduct());
        if (response.workflowId() != null) {
            Link retrieval = SellerWorkflowApiLinks.getCreateSellableProduct(response.workflowId());
            model.add(canonicalRead ? retrieval.withSelfRel() : retrieval);
        }
        return model;
    }
}
