package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/catalog")
public class SellerCatalogRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(linkTo(methodOn(SellerCatalogRootController.class).root()).withSelfRel());

        model.add(linkTo(methodOn(SellerProductController.class).searchProducts(null, null, 0, 20))
                .withRel("search-products"));
        model.add(linkTo(methodOn(SellerProductController.class).getProduct(null, null))
                .withRel("get-product"));
        model.add(linkTo(methodOn(SellerProductController.class).createProduct(null, null))
                .withRel("create-product"));

        model.add(linkTo(methodOn(SellerCategoryController.class).getLeafNodes(null))
                .withRel("search-category-leaves"));
        model.add(linkTo(methodOn(SellerCategoryController.class).getCategoryTree(null))
                .withRel("get-category-tree"));

        model.add(linkTo(methodOn(SellerVariantOptionController.class).listVariantOptions(null, null))
                .withRel("search-variant-options"));
        model.add(linkTo(methodOn(SellerVariantOptionController.class).listVariantTypes(null))
                .withRel("search-variant-types"));

        model.add(linkTo(methodOn(SellerSellableProductWorkflowController.class).startCreateSellableProduct(null, null))
                .withRel("create-sellable-product"));
        model.add(linkTo(methodOn(SellerSellableProductWorkflowController.class).startUpdateSellableProduct(null, null))
                .withRel("update-sellable-product"));
        model.add(linkTo(methodOn(SellerSellableProductWorkflowController.class).startUpdateProductVariant(null, null))
                .withRel("update-product-variant"));

        return ResponseEntity.ok(model);
    }
}
