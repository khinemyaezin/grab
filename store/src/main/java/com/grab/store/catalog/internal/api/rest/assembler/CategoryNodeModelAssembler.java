package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.dto.response.CategoryNodeResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class CategoryNodeModelAssembler implements RepresentationModelAssembler<CategoryNodeResponse, EntityModel<CategoryNodeResponse>> {

    @Override
    public EntityModel<CategoryNodeResponse> toModel(CategoryNodeResponse response) {
        String id = response.id();
        return EntityModel.of(response,
                Link.of("/api/v1/categories/" + id + "/tree").withSelfRel(),
                Link.of("/api/v1/categories/" + id).withRel("category")
        );
    }
}
