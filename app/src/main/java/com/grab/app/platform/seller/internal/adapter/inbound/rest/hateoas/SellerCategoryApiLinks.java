package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerCategoryController;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class SellerCategoryApiLinks {

    private SellerCategoryApiLinks() {
    }

    public static Link getCategoryTree(String rootId) {
        return linkTo(methodOn(SellerCategoryController.class).getCategoryTree(rootId))
                .withRel("get-category-tree");
    }

    public static Link getCategory(String categoryId) {
        return linkTo(methodOn(SellerCategoryController.class).getCategory(categoryId))
                .withRel("get-category");
    }

    public static Link getChildren(String categoryId) {
        return linkTo(methodOn(SellerCategoryController.class).getChildren(categoryId))
                .withRel("list-category-children");
    }

    public static Link deleteCategory(String categoryId) {
        return linkTo(methodOn(SellerCategoryController.class).deleteCategory(categoryId))
                .withRel("delete-category");
    }

    public static Link getLeafNodes(String name) {
        return linkTo(methodOn(SellerCategoryController.class).getLeafNodes(name))
                .withRel("search-category-leaves");
    }

    public static Link saveCategory() {
        return linkTo(methodOn(SellerCategoryController.class).saveCategory(null)).withRel("create-category");
    }
}
