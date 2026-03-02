package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.dto.response.CategoryResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class CategoryModelAssembler implements RepresentationModelAssembler<CategoryResponse, EntityModel<CategoryResponse>> {

    @Override
    public EntityModel<CategoryResponse> toModel(CategoryResponse response) {
        String id = response.id();
        return EntityModel.of(response,
                Link.of("/api/v1/categories/" + id).withSelfRel(),
                Link.of("/api/v1/categories/" + id + "/parent").withRel("parent"),
                Link.of("/api/v1/categories/" + id + "/children").withRel("children"),
                Link.of("/api/v1/categories/" + id + "/tree").withRel("tree")
        );
    }
}
