package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerPublishableKeyController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PublishableKeyResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PublishableKeyModelAssembler implements RepresentationModelAssembler<PublishableKeyResponse, EntityModel<PublishableKeyResponse>> {

    @Override
    public EntityModel<PublishableKeyResponse> toModel(PublishableKeyResponse key) {
        if (key == null) {
            return null;
        }
        EntityModel<PublishableKeyResponse> model = EntityModel.of(key);
        if (key.salesChannelId() != null && key.keyId() != null) {
            model.add(linkTo(methodOn(SellerPublishableKeyController.class).revokePublishableKey(key.salesChannelId(), key.keyId())).withRel("revoke-publishable-key"));
        }
        return model;
    }
}

