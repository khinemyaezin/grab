package com.grab.store.catalog;

import com.grab.store.catalog.internal.api.rest.controller.CategoryController;
import com.grab.store.catalog.internal.api.rest.controller.ProductController;
import com.grab.store.catalog.internal.api.rest.controller.VariantOptionController;
import com.grab.store.catalog.internal.api.rest.controller.VariantTypeController;
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
public interface CatalogRootApi {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    default ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();

        model.add(linkTo(methodOn(CatalogRootApi.class)
                .root())
                .withSelfRel());

        model.add(linkTo(methodOn(ProductController.class)
                .getProducts(null))
                .withRel("paged-product"));

        model.add(linkTo(methodOn(CategoryController.class)
                .getLeafNodesByName(null))
                .withRel("paged-category"));

        model.add(linkTo(methodOn(VariantTypeController.class)
                .getVariantTypesByName(null))
                .withRel("paged-variant-type"));

        model.add(linkTo(methodOn(VariantOptionController.class)
                .getVariantOptionsByName(null, null))
                .withRel("paged-variant-option"));
        return ResponseEntity.ok(model);
    }
}
