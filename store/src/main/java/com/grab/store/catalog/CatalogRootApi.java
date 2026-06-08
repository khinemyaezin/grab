package com.grab.store.catalog;

import com.grab.store.shared.LinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog")
public interface CatalogRootApi {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    default ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(Link.of("/api/v1/catalog").withSelfRel());
        model.add(Link.of("/api/v1/catalog/products").withRel(LinkRelations.PRODUCTS));
        model.add(Link.of("/api/v1/catalog/categories").withRel(LinkRelations.CATEGORIES));
        model.add(Link.of("/api/v1/catalog/variant-types").withRel(LinkRelations.VARIANT_TYPES));
        model.add(Link.of("/api/v1/catalog/variant-options").withRel(LinkRelations.VARIANT_OPTIONS));
        return ResponseEntity.ok(model);
    }
}
