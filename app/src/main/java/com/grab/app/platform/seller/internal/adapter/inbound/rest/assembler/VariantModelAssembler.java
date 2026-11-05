package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerProductController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.VariantResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class VariantModelAssembler implements RepresentationModelAssembler<VariantResponse, EntityModel<VariantResponse>> {

    public EntityModel<VariantResponse> toModel(VariantResponse variant, String productId) {
        if (variant == null) {
            return null;
        }
        EntityModel<VariantResponse> model = EntityModel.of(variant);
        if (productId != null && variant.id() != null) {
            model.add(linkTo(methodOn(SellerProductController.class).getVariant(productId, variant.id(), null)).withSelfRel());
            model.add(linkTo(methodOn(SellerProductController.class).getProduct(productId, null)).withRel("get-product"));
        }
        return model;
    }

    @Override
    public EntityModel<VariantResponse> toModel(VariantResponse variant) {
        return toModel(variant, null);
    }
}
