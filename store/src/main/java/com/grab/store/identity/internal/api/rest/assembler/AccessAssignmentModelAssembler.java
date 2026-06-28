package com.grab.store.identity.internal.api.rest.assembler;

import com.grab.store.identity.internal.api.rest.controller.AccessAdminController;
import com.grab.store.identity.internal.api.rest.controller.UserAdminController;
import com.grab.store.identity.internal.api.rest.dto.response.AccessAssignmentResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AccessAssignmentModelAssembler
        implements RepresentationModelAssembler<AccessAssignmentResponse, EntityModel<AccessAssignmentResponse>> {
    @Override
    public EntityModel<AccessAssignmentResponse> toModel(AccessAssignmentResponse response) {
        EntityModel<AccessAssignmentResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(UserAdminController.class)
                .getUser(response.userId()))
                .withRel("get-user"));
        if ("ACTIVE".equals(response.status())) {
            model.add(linkTo(methodOn(AccessAdminController.class)
                    .suspend(response.id(), null))
                    .withRel("suspend-access-assignment"));
            model.add(linkTo(methodOn(AccessAdminController.class)
                    .revoke(response.id(), null))
                    .withRel("revoke-access-assignment"));
        } else if ("SUSPENDED".equals(response.status())) {
            model.add(linkTo(methodOn(AccessAdminController.class)
                    .reactivate(response.id(), null))
                    .withRel("reactivate-access-assignment"));
            model.add(linkTo(methodOn(AccessAdminController.class)
                    .revoke(response.id(), null))
                    .withRel("revoke-access-assignment"));
        }
        return model;
    }
}
