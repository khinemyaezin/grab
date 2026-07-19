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
public class CatalogRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();

        model.add(linkTo(methodOn(CatalogRootController.class)
                .root())
                .withSelfRel());

        model.add(linkTo(methodOn(ProductController.class)
                .searchProducts(null, null, null))
                .withRel("search-products"));

        model.add(linkTo(methodOn(ProductController.class)
                .searchProductVariants(null, null, null))
                .withRel("search-product-variants"));

        model.add(linkTo(methodOn(ProductController.class)
                .getProduct(null))
                .withRel("get-product"));


        model.add(linkTo(methodOn(ProductController.class)
                .saveProduct(null))
                .withRel("create-product"));

        model.add(linkTo(methodOn(ProductController.class).getVariationMatrix(null))
                .withRel("generate-variation-matrix"));

        model.add(linkTo(methodOn(CategoryController.class).getLeafNodesByName(null))
                .withRel("search-category-leaves"));

        model.add(linkTo(methodOn(VariantTypeController.class)
                .getVariantTypesByName(null))
                .withRel("search-variant-types"));

        model.add(linkTo(methodOn(VariantOptionController.class)
                .getVariantOptionsByName(null, null))
                .withRel("search-variant-options"));

        return ResponseEntity.ok(model);
    }
}
