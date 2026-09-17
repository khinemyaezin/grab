package com.grab.store.storefront.internal.api.rest.assembler;

import com.grab.store.storefront.internal.api.rest.controller.StorefrontCategoryController;
import com.grab.store.storefront.internal.api.rest.dto.response.StorefrontCategoryChildrenResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class StorefrontCategoryChildrenModelAssembler
        implements RepresentationModelAssembler<StorefrontCategoryChildrenResponse, EntityModel<StorefrontCategoryChildrenResponse>> {

    @Override
    public EntityModel<StorefrontCategoryChildrenResponse> toModel(StorefrontCategoryChildrenResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(StorefrontCategoryController.class).children(response.parentId())).withSelfRel(),
                linkTo(methodOn(StorefrontCategoryController.class).tree(response.parentId())).withRel("get-category-tree"));
    }
}
