package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.controller.CategoryController;
import com.grab.store.catalog.internal.api.rest.dto.response.CategoryResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CategoryModelAssembler implements RepresentationModelAssembler<CategoryResponse, EntityModel<CategoryResponse>> {

    @Override
    public EntityModel<CategoryResponse> toModel(CategoryResponse response) {
        String id = response.id();
        EntityModel<CategoryResponse> entity = EntityModel.of(response);

        entity.add(linkTo(methodOn(CategoryController.class).getCategory(id)).withSelfRel());
        entity.add(linkTo(methodOn(CategoryController.class).getCategoryParent(id)).withRel("get-category-parent"));
        entity.add(linkTo(methodOn(CategoryController.class).getCategoryChildren(id)).withRel("list-category-children"));
        entity.add(linkTo(methodOn(CategoryController.class).getCategoryTree(id)).withRel("get-category-tree"));

        entity.add(linkTo(methodOn(CategoryController.class).deleteCategory(id)).withRel("delete-category"));

        return entity;
    }
}
