package com.grab.app.channeldiscovery.storefrontquery;

import com.grab.app.channeldiscovery.storefrontquery.internal.api.rest.controller.StorefrontProductController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/storefront-query")
public class StorefrontQueryRootController {

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> model = new RepresentationModel<>();

        Link selfLink = linkTo(methodOn(StorefrontQueryRootController.class).root())
                .withSelfRel();
        model.add(selfLink);

        Link listProductsLink = linkTo(methodOn(StorefrontProductController.class).list(null, null, null, null, null, null, null))
                .withRel("list-storefront-products");
        model.add(listProductsLink);

        Link getProductLink = linkTo(methodOn(StorefrontProductController.class).getBySlug(null, null, null, null))
                .withRel("get-storefront-product");
        model.add(getProductLink);

        Link resolveContextLink = linkTo(methodOn(StorefrontProductController.class).resolveContext(null, null, null))
                .withRel("resolve-shopping-context");
        model.add(resolveContextLink);

        return ResponseEntity.ok(model);
    }
}
