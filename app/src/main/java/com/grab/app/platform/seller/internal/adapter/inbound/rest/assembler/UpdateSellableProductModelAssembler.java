package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerSellableProductWorkflowController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateSellableProductResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UpdateSellableProductModelAssembler
        implements RepresentationModelAssembler<UpdateSellableProductResponse, EntityModel<UpdateSellableProductResponse>> {

    @Override
    public EntityModel<UpdateSellableProductResponse> toModel(UpdateSellableProductResponse response) {
        if (response == null) {
            return null;
        }
        return EntityModel.of(
                response,
                linkTo(methodOn(SellerSellableProductWorkflowController.class).getUpdateSellableProduct(response.workflowId()))
                        .withSelfRel(),
                linkTo(methodOn(SellerSellableProductWorkflowController.class).getUpdateSellableProduct(response.workflowId()))
                        .withRel("get-update-sellable-product"),
                linkTo(methodOn(SellerSellableProductWorkflowController.class).startUpdateSellableProduct(null, null))
                        .withRel("update-sellable-product")
        );
    }
}

