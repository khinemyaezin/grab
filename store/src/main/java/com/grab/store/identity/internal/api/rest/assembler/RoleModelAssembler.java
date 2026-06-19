package com.grab.store.identity.internal.api.rest.assembler;

import com.grab.store.identity.internal.api.rest.controller.IdentityAdminController;
import com.grab.store.identity.internal.api.rest.dto.response.RoleResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class RoleModelAssembler implements RepresentationModelAssembler<RoleResponse, EntityModel<RoleResponse>> {
    @Override
    public EntityModel<RoleResponse> toModel(RoleResponse response) {
        EntityModel<RoleResponse> entity = EntityModel.of(response);
        entity.add(
            linkTo(methodOn(IdentityAdminController.class)
            .roles())
            .withRel("list-roles"));
        return entity;
    }
}
