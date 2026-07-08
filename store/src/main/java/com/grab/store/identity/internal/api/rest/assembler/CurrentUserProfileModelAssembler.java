package com.grab.store.identity.internal.api.rest.assembler;

import com.grab.store.identity.internal.api.rest.controller.ProfileController;
import com.grab.store.identity.internal.api.rest.dto.response.CurrentUserProfileResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CurrentUserProfileModelAssembler
        implements RepresentationModelAssembler<CurrentUserProfileResponse, EntityModel<CurrentUserProfileResponse>> {

    @Override
    public EntityModel<CurrentUserProfileResponse> toModel(CurrentUserProfileResponse response) {
        return EntityModel.of(
                response,
                linkTo(methodOn(ProfileController.class).getProfile(null)).withSelfRel()
        );
    }
}
