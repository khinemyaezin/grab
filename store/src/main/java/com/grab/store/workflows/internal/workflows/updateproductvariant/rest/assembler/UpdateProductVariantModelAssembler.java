package com.grab.store.workflows.internal.workflows.updateproductvariant.rest.assembler;

import com.grab.store.workflows.internal.workflows.updateproductvariant.rest.controller.UpdateProductVariantController;
import com.grab.store.workflows.internal.workflows.updateproductvariant.rest.dto.response.UpdateProductVariantResponse;
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
        return EntityModel.of(
                response,
                linkTo(methodOn(UpdateProductVariantController.class).get(response.workflowId(), null))
                        .withSelfRel(),
                linkTo(methodOn(UpdateProductVariantController.class).get(response.workflowId(), null))
                        .withRel("get-update-product-variant"),
                linkTo(methodOn(UpdateProductVariantController.class).start(null, null))
                        .withRel("update-product-variant")
        );
    }
}
