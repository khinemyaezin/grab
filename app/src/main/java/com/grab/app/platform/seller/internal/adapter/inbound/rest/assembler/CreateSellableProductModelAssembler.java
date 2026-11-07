package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerSellableProductWorkflowController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CreateSellableProductResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CreateSellableProductModelAssembler
        implements RepresentationModelAssembler<CreateSellableProductResponse, EntityModel<CreateSellableProductResponse>> {

    @Override
    public EntityModel<CreateSellableProductResponse> toModel(CreateSellableProductResponse response) {
        if (response == null) {
            return null;
        }
        return EntityModel.of(
                response,
                linkTo(methodOn(SellerSellableProductWorkflowController.class).getCreateSellableProduct(response.workflowId()))
                        .withSelfRel(),
                linkTo(methodOn(SellerSellableProductWorkflowController.class).getCreateSellableProduct(response.workflowId()))
                        .withRel("get-create-sellable-product"),
                linkTo(methodOn(SellerSellableProductWorkflowController.class).startCreateSellableProduct(null, null))
                        .withRel("create-sellable-product")
        );
    }
}

