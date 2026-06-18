package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.controller.CategoryController;
import com.grab.store.catalog.internal.api.rest.dto.response.CategoryLeavesResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CategoryLeavesModelAssembler implements RepresentationModelAssembler<CategoryLeavesResponse, EntityModel<CategoryLeavesResponse>> {

    public EntityModel<CategoryLeavesResponse> toModel(CategoryLeavesResponse response, String name) {
        return EntityModel.of(response,
                linkTo(methodOn(CategoryController.class).getLeafNodesByName(name)).withSelfRel()
        );
    }

    @Override
    public EntityModel<CategoryLeavesResponse> toModel(CategoryLeavesResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(CategoryController.class).getLeafNodesByName(null)).withRel("search-category-leaves")
        );
    }
}
