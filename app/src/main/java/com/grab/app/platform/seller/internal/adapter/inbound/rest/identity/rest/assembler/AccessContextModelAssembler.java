package com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.AccessContextController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.AccessContextResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AccessContextModelAssembler
        implements RepresentationModelAssembler<AccessContextResponse, EntityModel<AccessContextResponse>> {
    @Override
    public EntityModel<AccessContextResponse> toModel(AccessContextResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(AccessContextController.class)
                        .selectContext(response.assignmentId(), null, null))
                        .withRel("select-access-context")
        );
    }
}
