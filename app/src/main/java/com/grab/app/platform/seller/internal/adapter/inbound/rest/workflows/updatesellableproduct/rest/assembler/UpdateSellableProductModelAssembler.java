package com.grab.app.platform.seller.internal.adapter.inbound.rest.workflows.updatesellableproduct.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.UpdateSellableProductController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.workflows.updatesellableproduct.dto.response.UpdateSellableProductResponse;
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
        return EntityModel.of(
                response,
                linkTo(methodOn(UpdateSellableProductController.class).get(response.workflowId(), null))
                        .withSelfRel(),
                linkTo(methodOn(UpdateSellableProductController.class).get(response.workflowId(), null))
                        .withRel("get-update-sellable-product"),
                linkTo(methodOn(UpdateSellableProductController.class).start(null, null))
                        .withRel("update-sellable-product")
        );
    }
}
