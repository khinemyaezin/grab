package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PublishableKeyResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerPublishableKeyApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class PublishableKeyModelAssembler implements RepresentationModelAssembler<PublishableKeyResponse, EntityModel<PublishableKeyResponse>> {

    @Override
    public EntityModel<PublishableKeyResponse> toModel(PublishableKeyResponse key) {
        if (key == null) {
            return null;
        }
        EntityModel<PublishableKeyResponse> model = EntityModel.of(key);
        if (key.salesChannelId() != null && key.keyId() != null) {
            model.add(SellerPublishableKeyApiLinks.revokePublishableKey(key.salesChannelId(), key.keyId()));
        }
        return model;
    }
}

