package com.grab.app.platform.seller.internal.adapter.inbound.rest.workflows.updateproductvariant.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.UpdateProductVariantController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.workflows.updateproductvariant.dto.response.UpdateProductVariantResponse;
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
