package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.CategoryController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.CategoryNodeResponse;
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
