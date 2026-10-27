package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.CategoryController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.DeleteCategoryResponse;
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
