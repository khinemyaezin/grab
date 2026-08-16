package com.grab.store.workflows.internal.updatesellableproduct.rest.assembler;

import com.grab.store.workflows.internal.updatesellableproduct.rest.controller.UpdateSellableProductController;
import com.grab.store.workflows.internal.updatesellableproduct.rest.dto.response.UpdateSellableProductResponse;
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
