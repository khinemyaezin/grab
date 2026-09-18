package com.grab.store.workflows.internal.workflows.publishproducttochannel.rest.assembler;

import com.grab.store.workflows.internal.workflows.publishproducttochannel.rest.controller.PublishProductToChannelController;
import com.grab.store.workflows.internal.workflows.publishproducttochannel.rest.dto.response.PublishProductToChannelResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PublishProductToChannelModelAssembler
        implements RepresentationModelAssembler<PublishProductToChannelResponse, EntityModel<PublishProductToChannelResponse>> {

    @Override
    public EntityModel<PublishProductToChannelResponse> toModel(PublishProductToChannelResponse response) {
        return EntityModel.of(
                response,
                linkTo(methodOn(PublishProductToChannelController.class).get(response.workflowId(), null))
                        .withSelfRel(),
                linkTo(methodOn(PublishProductToChannelController.class).get(response.workflowId(), null))
                        .withRel("get-publish-product-to-channel"),
                linkTo(methodOn(PublishProductToChannelController.class).start(null, null))
                        .withRel("publish-product-to-channel")
        );
    }
}
