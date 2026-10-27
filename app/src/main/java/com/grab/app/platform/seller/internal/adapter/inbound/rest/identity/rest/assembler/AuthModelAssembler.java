package com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.AccessContextController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.AuthController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.ProfileController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.AuthResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AuthModelAssembler implements RepresentationModelAssembler<AuthResponse, EntityModel<AuthResponse>> {

    @Override
    public EntityModel<AuthResponse> toModel(AuthResponse response) {
        EntityModel<AuthResponse> entity = EntityModel.of(response);
        entity.add(linkTo(methodOn(AuthController.class)
                .login(null))
                .withRel("login"));

        if (response.accessToken() != null && !response.accessToken().isBlank()) {
            entity.add(linkTo(methodOn(ProfileController.class)
                    .getProfile(null))
                    .withRel("get-profile"));
        }
        if (response.contextSelectionRequired()) {
            entity.add(linkTo(methodOn(AccessContextController.class)
                    .listContexts(null))
                    .withRel("select-access-context"));
        }
        return entity;
    }
}
