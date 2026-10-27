package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.CategoryController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.CategoryResponse;
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
