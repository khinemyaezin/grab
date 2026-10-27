package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.ProductController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.SyncVariantsResponse;
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
                linkTo(methodOn(ProductController.class).getProduct(response.productId())).withRel("get-product")
        );
    }
}
