package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateProductVariantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerWorkflowApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class UpdateProductVariantModelAssembler
        implements RepresentationModelAssembler<UpdateProductVariantResponse, EntityModel<UpdateProductVariantResponse>> {

    @Override
    public EntityModel<UpdateProductVariantResponse> toModel(UpdateProductVariantResponse response) {
        return assemble(response, true);
    }

    public EntityModel<UpdateProductVariantResponse> toCommandModel(UpdateProductVariantResponse response) {
        return assemble(response, false);
    }

    private EntityModel<UpdateProductVariantResponse> assemble(UpdateProductVariantResponse response, boolean canonicalRead) {
        if (response == null) {
            return null;
        }
        EntityModel<UpdateProductVariantResponse> model = EntityModel.of(response, SellerWorkflowApiLinks.startUpdateProductVariant());
        if (response.workflowId() != null) {
            Link retrieval = SellerWorkflowApiLinks.getUpdateProductVariant(response.workflowId());
            model.add(canonicalRead ? retrieval.withSelfRel() : retrieval);
        }
        return model;
    }
}
