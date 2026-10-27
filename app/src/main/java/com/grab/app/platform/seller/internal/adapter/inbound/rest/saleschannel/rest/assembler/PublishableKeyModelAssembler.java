package com.grab.app.platform.seller.internal.adapter.inbound.rest.saleschannel.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.PublishableKeyController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.saleschannel.rest.dto.response.PublishableKeyResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PublishableKeyModelAssembler
        implements RepresentationModelAssembler<PublishableKeyResponse, EntityModel<PublishableKeyResponse>> {
    @Override
    public EntityModel<PublishableKeyResponse> toModel(PublishableKeyResponse response) {
        return EntityModel.of(
                response,
                linkTo(methodOn(PublishableKeyController.class)
                        .list(response.salesChannelId(), null, null)).withRel("list-publishable-keys"),
                linkTo(methodOn(PublishableKeyController.class)
                        .revoke(response.salesChannelId(), response.keyId())).withRel("revoke-publishable-key")
        );
    }
}
