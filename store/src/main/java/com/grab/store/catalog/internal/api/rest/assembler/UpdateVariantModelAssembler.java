package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.controller.ProductController;
import com.grab.store.catalog.internal.api.rest.dto.response.UpdateVariantResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UpdateVariantModelAssembler implements RepresentationModelAssembler<UpdateVariantResponse, EntityModel<UpdateVariantResponse>> {

    @Override
    public EntityModel<UpdateVariantResponse> toModel(UpdateVariantResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(ProductController.class).getProduct(response.productId())).withRel("get-product")
        );
    }
}
