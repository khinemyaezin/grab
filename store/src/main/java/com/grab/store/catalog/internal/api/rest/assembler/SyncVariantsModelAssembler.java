package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.controller.ProductController;
import com.grab.store.catalog.internal.api.rest.dto.response.SyncVariantsResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SyncVariantsModelAssembler implements RepresentationModelAssembler<SyncVariantsResponse, EntityModel<SyncVariantsResponse>> {

    @Override
    public EntityModel<SyncVariantsResponse> toModel(SyncVariantsResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(ProductController.class).syncVariants(response.productId(), null)).withSelfRel(),
                linkTo(methodOn(ProductController.class).getProduct(response.productId())).withRel("product")
        );
    }
}
