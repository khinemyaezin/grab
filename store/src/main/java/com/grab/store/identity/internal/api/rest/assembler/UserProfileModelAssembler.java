package com.grab.store.identity.internal.api.rest.assembler;

import com.grab.store.identity.internal.api.rest.controller.UserAdminController;
import com.grab.store.identity.internal.api.rest.controller.RoleAdminController;
import com.grab.store.identity.internal.api.rest.dto.response.UserProfileResponse;
import com.identity.domain.enums.UserStatus;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UserProfileModelAssembler
        implements RepresentationModelAssembler<UserProfileResponse, EntityModel<UserProfileResponse>> {

    @Override
    public EntityModel<UserProfileResponse> toModel(UserProfileResponse response) {
        EntityModel<UserProfileResponse> entity = EntityModel.of(response);
        entity.add(linkTo(methodOn(UserAdminController.class)
                .getUser(response.id()))
                .withSelfRel());
        entity.add(linkTo(methodOn(RoleAdminController.class)
                .listRoles(null, null))
                .withRel("list-roles"));

        if (UserStatus.ACTIVE.name().equals(response.status())) {
            entity.add(linkTo(methodOn(UserAdminController.class)
                    .suspendUser(response.id()))
                    .withRel("suspend-user"));
        } else if (UserStatus.PENDING_APPROVAL.name().equals(response.status())) {
            entity.add(linkTo(methodOn(UserAdminController.class)
                    .approveUser(response.id()))
                    .withRel("approve-user"));
        } else if (UserStatus.SUSPENDED.name().equals(response.status())) {
            entity.add(linkTo(methodOn(UserAdminController.class)
                    .reactivateUser(response.id()))
                    .withRel("reactivate-user"));
        }
        return entity;
    }
}
