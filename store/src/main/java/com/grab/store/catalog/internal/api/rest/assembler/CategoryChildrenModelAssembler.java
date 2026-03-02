package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.dto.response.CategoryChildrenResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class CategoryChildrenModelAssembler implements RepresentationModelAssembler<CategoryChildrenResponse, EntityModel<CategoryChildrenResponse>> {

    @Override
    public EntityModel<CategoryChildrenResponse> toModel(CategoryChildrenResponse response) {
        String id = response.parentId();
        return EntityModel.of(response,
                Link.of("/api/v1/categories/" + id + "/children").withSelfRel(),
                Link.of("/api/v1/categories/" + id).withRel("category"),
                Link.of("/api/v1/categories/" + id + "/tree").withRel("tree")
        );
    }
}
