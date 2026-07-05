package com.grab.store.identity.internal.api.rest.assembler;

import com.grab.store.identity.internal.api.rest.controller.RoleAdminController;
import com.grab.store.identity.internal.api.rest.dto.response.RoleResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.Locale;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class RoleModelAssembler implements RepresentationModelAssembler<RoleResponse, EntityModel<RoleResponse>> {

    @Override
    public EntityModel<RoleResponse> toModel(RoleResponse response) {
        EntityModel<RoleResponse> model = EntityModel.of(
                response,
                linkTo(methodOn(RoleAdminController.class)
                        .listRoles(null, null))
                        .withRel("list-roles")
        );
        if ("CUSTOM".equals(response.kind()) && response.authorities().size() > 1) {
            for (String authority : response.authorities()) {
                String qualifier = authority.toLowerCase(Locale.ROOT).replace('_', '-');
                model.add(linkTo(methodOn(RoleAdminController.class)
                        .revokeAuthority(response.code(), authority))
                        .withRel("revoke-role-authority-" + qualifier));
            }
        }
        return model;
    }
}
