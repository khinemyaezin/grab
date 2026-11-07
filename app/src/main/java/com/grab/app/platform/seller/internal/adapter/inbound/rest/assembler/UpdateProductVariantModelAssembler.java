package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerSellableProductWorkflowController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateProductVariantResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UpdateProductVariantModelAssembler
        implements RepresentationModelAssembler<UpdateProductVariantResponse, EntityModel<UpdateProductVariantResponse>> {

    @Override
    public EntityModel<UpdateProductVariantResponse> toModel(UpdateProductVariantResponse response) {
        if (response == null) {
            return null;
        }
        return EntityModel.of(
                response,
                linkTo(methodOn(SellerSellableProductWorkflowController.class).getUpdateProductVariant(response.workflowId()))
                        .withSelfRel(),
                linkTo(methodOn(SellerSellableProductWorkflowController.class).getUpdateProductVariant(response.workflowId()))
                        .withRel("get-update-product-variant"),
                linkTo(methodOn(SellerSellableProductWorkflowController.class).startUpdateProductVariant(null, null))
                        .withRel("update-product-variant")
        );
    }
}

