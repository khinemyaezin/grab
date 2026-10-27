package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.CategoryController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.CategoryChildrenResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CategoryChildrenModelAssembler implements RepresentationModelAssembler<CategoryChildrenResponse, EntityModel<CategoryChildrenResponse>> {

    @Override
    public EntityModel<CategoryChildrenResponse> toModel(CategoryChildrenResponse response) {
        String id = response.parentId();
        return EntityModel.of(response,
                linkTo(methodOn(CategoryController.class).getCategoryChildren(id)).withSelfRel(),
                linkTo(methodOn(CategoryController.class).getCategory(id)).withRel("get-category"),
                linkTo(methodOn(CategoryController.class).getCategoryTree(id)).withRel("get-category-tree")
        );
    }
}
