package com.grab.store.identity.internal.api.rest.assembler;

import com.grab.store.identity.internal.api.rest.controller.IdentityAdminController;
import com.grab.store.identity.internal.api.rest.dto.response.UserProfileResponse;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class UserProfileModelAssembler implements RepresentationModelAssembler<UserProfileResponse, EntityModel<UserProfileResponse>> {
    @Override
    public EntityModel<UserProfileResponse> toModel(UserProfileResponse response) {
        return EntityModel.of(response, linkTo(methodOn(IdentityAdminController.class).getUser(response.id())).withSelfRel());
    }
}
