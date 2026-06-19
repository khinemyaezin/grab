package com.grab.store.identity.internal.api.rest.assembler;

import com.grab.store.identity.internal.api.rest.controller.RoleAdminController;
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
        return EntityModel.of(
                response,
                linkTo(methodOn(RoleAdminController.class)
                        .listRoles(null, null))
                        .withRel("list-roles")
        );
    }
}
