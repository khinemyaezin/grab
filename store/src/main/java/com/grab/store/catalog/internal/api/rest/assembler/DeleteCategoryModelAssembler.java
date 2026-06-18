package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.controller.CategoryController;
import com.grab.store.catalog.internal.api.rest.dto.response.DeleteCategoryResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class DeleteCategoryModelAssembler implements RepresentationModelAssembler<DeleteCategoryResponse, EntityModel<DeleteCategoryResponse>> {

    @Override
    public EntityModel<DeleteCategoryResponse> toModel(DeleteCategoryResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(CategoryController.class).saveCategory(null)).withRel("create-category")
        );
    }
}
