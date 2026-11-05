package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerCategoryController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CategoryResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CategoryModelAssembler implements RepresentationModelAssembler<CategoryResponse, EntityModel<CategoryResponse>> {

    @Override
    public EntityModel<CategoryResponse> toModel(CategoryResponse category) {
        if (category == null) {
            return null;
        }
        EntityModel<CategoryResponse> model = EntityModel.of(category);
        if (category.id() != null) {
            model.add(linkTo(methodOn(SellerCategoryController.class).getCategory(category.id())).withSelfRel());
            model.add(linkTo(methodOn(SellerCategoryController.class).getChildren(category.id())).withRel("get-category-children"));
            model.add(linkTo(methodOn(SellerCategoryController.class).deleteCategory(category.id())).withRel("delete-category"));
        }
        model.add(linkTo(methodOn(SellerCategoryController.class).getCategoryTree(null)).withRel("get-category-tree"));
        return model;
    }
}
