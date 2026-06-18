package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.controller.CategoryController;
import com.grab.store.catalog.internal.api.rest.dto.response.CategoryNodeResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CategoryNodeModelAssembler implements RepresentationModelAssembler<CategoryNodeResponse, EntityModel<CategoryNodeResponse>> {

    @Override
    public EntityModel<CategoryNodeResponse> toModel(CategoryNodeResponse response) {
        String id = response.id();
        return EntityModel.of(response,
                linkTo(methodOn(CategoryController.class).getCategoryTree(id)).withSelfRel(),
                linkTo(methodOn(CategoryController.class).getCategory(id)).withRel("get-category")
        );
    }
}
