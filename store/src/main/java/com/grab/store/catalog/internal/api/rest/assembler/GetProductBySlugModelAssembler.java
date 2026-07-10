package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.controller.ProductController;
import com.grab.store.catalog.internal.api.rest.controller.CategoryController;
import com.grab.store.catalog.internal.api.rest.dto.response.GetProductBySlugResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class GetProductBySlugModelAssembler
        implements RepresentationModelAssembler<GetProductBySlugResponse, EntityModel<GetProductBySlugResponse>> {

    @Override
    public EntityModel<GetProductBySlugResponse> toModel(GetProductBySlugResponse response) {
        EntityModel<GetProductBySlugResponse> entity = EntityModel.of(response);

        entity.add(linkTo(methodOn(ProductController.class).getProductBySlug(response.slug())).withSelfRel());
        entity.add(linkTo(methodOn(ProductController.class).getProduct(response.id())).withRel("get-product"));
        entity.add(linkTo(methodOn(ProductController.class).getProducts(null, null, null)).withRel("search-products"));
        entity.add(linkTo(methodOn(CategoryController.class).getCategory(response.categoryId())).withRel("get-category"));

        entity.add(linkTo(methodOn(ProductController.class).updateProduct(response.id(), null)).withRel("update-product"));
        entity.add(linkTo(methodOn(ProductController.class).updateProductStatus(response.id(), null)).withRel("update-product-status"));
        entity.add(linkTo(methodOn(ProductController.class).deleteProduct(response.id())).withRel("delete-product"));

        entity.add(linkTo(methodOn(ProductController.class).publish(response.id(), null)).withRel("publish-product"));
        entity.add(linkTo(methodOn(ProductController.class).suspend(response.id(), null)).withRel("suspend-product"));
        entity.add(linkTo(methodOn(ProductController.class).restore(response.id(), null)).withRel("restore-product"));

        return entity;
    }
}
