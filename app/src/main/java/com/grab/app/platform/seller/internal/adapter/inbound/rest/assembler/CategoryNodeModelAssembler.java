package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerCategoryController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CategoryNodeResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CategoryNodeModelAssembler implements RepresentationModelAssembler<CategoryNodeResponse, EntityModel<CategoryNodeResponse>> {

    @Override
    public EntityModel<CategoryNodeResponse> toModel(CategoryNodeResponse node) {
        if (node == null) {
            return null;
        }
        EntityModel<CategoryNodeResponse> model = EntityModel.of(node);
        model.add(linkTo(methodOn(SellerCategoryController.class).getCategoryTree(node.id())).withSelfRel());
        return model;
    }
}
