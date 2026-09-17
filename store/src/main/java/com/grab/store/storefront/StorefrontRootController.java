package com.grab.store.storefront;

import com.grab.store.storefront.internal.api.rest.controller.StorefrontCategoryController;
import com.grab.store.storefront.internal.api.rest.controller.StorefrontProductController;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/storefront")
public class StorefrontRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(linkTo(methodOn(StorefrontRootController.class).root()).withSelfRel());
        model.add(linkTo(methodOn(StorefrontCategoryController.class).tree(null)).withRel("get-category-tree"));
        model.add(linkTo(methodOn(StorefrontCategoryController.class).children(null)).withRel("get-category-children"));
        model.add(linkTo(methodOn(StorefrontProductController.class).search(null, null, null))
                .withRel("search-products"));
        model.add(linkTo(methodOn(StorefrontProductController.class).featured(null, null))
                .withRel("list-featured-products"));
        model.add(linkTo(methodOn(StorefrontProductController.class).newArrivals(null, null))
                .withRel("list-new-arrivals"));
        model.add(linkTo(methodOn(StorefrontProductController.class).bySlug(null))
                .withRel("get-product-by-slug"));
        return ResponseEntity.ok(model);
    }
}
