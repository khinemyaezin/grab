package com.grab.store.identity.internal.api.rest.assembler;

import com.grab.store.identity.internal.api.rest.controller.RoleAdminController;
import com.grab.store.identity.internal.api.rest.dto.response.RoleSearchResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SearchRolesModelAssembler
        implements RepresentationModelAssembler<RoleSearchResponse, EntityModel<RoleSearchResponse>> {

    @Override
    public EntityModel<RoleSearchResponse> toModel(RoleSearchResponse response) {
        return EntityModel.of(response);
    }

    public CollectionModel<EntityModel<RoleSearchResponse>> toCollectionModel(
            Iterable<? extends RoleSearchResponse> entities,
            String name
    ) {
        CollectionModel<EntityModel<RoleSearchResponse>> collection =
                RepresentationModelAssembler.super.toCollectionModel(entities);
        collection.add(linkTo(methodOn(RoleAdminController.class).searchRoles(name)).withSelfRel());
        return collection;
    }
}
