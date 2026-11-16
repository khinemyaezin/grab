package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.VariantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerProductApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class VariantModelAssembler implements RepresentationModelAssembler<VariantResponse, EntityModel<VariantResponse>> {

    public EntityModel<VariantResponse> toModel(VariantResponse variant, String productId) {
        if (variant == null) {
            return null;
        }
        EntityModel<VariantResponse> model = EntityModel.of(variant);
        if (productId != null && variant.id() != null) {
            model.add(SellerProductApiLinks.getVariant(productId, variant.id()).withSelfRel());
            model.add(SellerProductApiLinks.getProduct(productId));
        }
        return model;
    }

    @Override
    public EntityModel<VariantResponse> toModel(VariantResponse variant) {
        return toModel(variant, null);
    }
}
