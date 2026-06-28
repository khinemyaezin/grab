package com.grab.store.identity.internal.api.rest.assembler;

import com.grab.store.identity.internal.api.rest.controller.AccessInvitationController;
import com.grab.store.identity.internal.api.rest.dto.response.AccessInvitationResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AccessInvitationModelAssembler
        implements RepresentationModelAssembler<AccessInvitationResponse, EntityModel<AccessInvitationResponse>> {
    @Override
    public EntityModel<AccessInvitationResponse> toModel(AccessInvitationResponse response) {
        EntityModel<AccessInvitationResponse> model = EntityModel.of(response);
        if ("PENDING".equals(response.status())) {
            model.add(linkTo(methodOn(AccessInvitationController.class)
                    .accept(null, null))
                    .withRel("accept-access-invitation"));
            model.add(linkTo(methodOn(AccessInvitationController.class)
                    .cancel(response.id(), null))
                    .withRel("cancel-access-invitation"));
        }
        return model;
    }
}
