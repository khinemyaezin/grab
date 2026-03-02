package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.dto.response.DeleteCategoryResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class DeleteCategoryModelAssembler implements RepresentationModelAssembler<DeleteCategoryResponse, EntityModel<DeleteCategoryResponse>> {

    @Override
    public EntityModel<DeleteCategoryResponse> toModel(DeleteCategoryResponse response) {
        return EntityModel.of(response,
                Link.of("/api/v1/categories/" + response.id()).withSelfRel(),
                Link.of("/api/v1/categories").withRel("categories")
        );
    }
}
