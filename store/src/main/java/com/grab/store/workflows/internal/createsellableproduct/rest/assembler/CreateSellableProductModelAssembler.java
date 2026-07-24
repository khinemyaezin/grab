package com.grab.store.workflows.internal.createsellableproduct.rest.assembler;

import com.grab.store.workflows.internal.createsellableproduct.rest.controller.CreateSellableProductController;
import com.grab.store.workflows.internal.createsellableproduct.rest.dto.response.CreateSellableProductResponse;
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
        return EntityModel.of(
                response,
                linkTo(methodOn(CreateSellableProductController.class).get(response.workflowId(), null))
                        .withSelfRel(),
                linkTo(methodOn(CreateSellableProductController.class).get(response.workflowId(), null))
                        .withRel("get-create-sellable-product"),
                linkTo(methodOn(CreateSellableProductController.class).start(null, null))
                        .withRel("create-sellable-product")
        );
    }
}
