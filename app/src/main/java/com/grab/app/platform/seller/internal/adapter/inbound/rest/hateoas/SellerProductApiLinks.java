package com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerProductController;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public final class SellerProductApiLinks {

    private SellerProductApiLinks() {
    }

    public static Link getProduct(String productId) {
        return linkTo(methodOn(SellerProductController.class).getProduct(productId, null))
                .withRel("get-product");
    }

    public static Link getVariant(String productId, String variantId) {
        return linkTo(methodOn(SellerProductController.class).getVariant(productId, variantId, null))
                .withRel("get-variant");
    }

    public static Link updateProduct(String productId) {
        return linkTo(methodOn(SellerProductController.class).updateProduct(productId, null, null))
                .withRel("update-product");
    }

    public static Link deleteProduct(String productId) {
        return linkTo(methodOn(SellerProductController.class).deleteProduct(productId, null))
                .withRel("delete-product");
    }

    public static Link updateProductStatus(String productId) {
        return linkTo(methodOn(SellerProductController.class).updateProductStatus(productId, null, null))
                .withRel("update-product-status");
    }

    public static Link getProductBySlug(String slug) {
        return linkTo(methodOn(SellerProductController.class).getProductBySlug(slug))
                .withRel("get-product-by-slug");
    }

    public static Link searchProducts(String query, int page, int size) {
        return linkTo(methodOn(SellerProductController.class).searchProducts(null, query, page, size))
                .withRel("search-products");
    }

    public static Link createProduct() {
        return linkTo(methodOn(SellerProductController.class).createProduct(null, null))
                .withRel("create-product");
    }

    public static Link listProducts() {
        return searchProducts(null, 0, 20).expand().withRel("list-products");
    }
}
