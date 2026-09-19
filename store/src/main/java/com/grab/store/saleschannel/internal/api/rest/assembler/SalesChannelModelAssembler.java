package com.grab.store.saleschannel.internal.api.rest.assembler;

import com.grab.store.saleschannel.internal.api.rest.controller.SalesChannelController;
import com.grab.store.saleschannel.internal.api.rest.dto.response.SalesChannelResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SalesChannelModelAssembler
        implements RepresentationModelAssembler<SalesChannelResponse, EntityModel<SalesChannelResponse>> {

    @Override
    public EntityModel<SalesChannelResponse> toModel(SalesChannelResponse response) {
        return EntityModel.of(
                response,
                linkTo(methodOn(SalesChannelController.class).get(response.salesChannelId())).withSelfRel(),
                linkTo(methodOn(SalesChannelController.class).get(response.salesChannelId()))
                        .withRel("get-sales-channel"),
                linkTo(methodOn(SalesChannelController.class).list(null, null))
                        .withRel("list-sales-channels")
        );
    }
}
