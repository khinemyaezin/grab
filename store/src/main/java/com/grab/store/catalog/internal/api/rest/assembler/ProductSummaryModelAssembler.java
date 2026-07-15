package com.grab.store.catalog.internal.api.rest.assembler;

import com.catalog.domain.valueobject.ProductStatus;
import com.grab.store.catalog.internal.api.rest.controller.ProductController;
import com.grab.store.catalog.internal.api.rest.dto.response.ProductSearchResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProductSummaryModelAssembler
        implements RepresentationModelAssembler<ProductSearchResponse, EntityModel<ProductSearchResponse>> {

    @Override
    public EntityModel<ProductSearchResponse> toModel(ProductSearchResponse response) {
        EntityModel<ProductSearchResponse> entity = EntityModel.of(response);

        entity.add(linkTo(methodOn(ProductController.class).updateProduct(response.productId(), null)).withRel("update-product"));
        entity.add(linkTo(methodOn(ProductController.class).updateProductStatus(response.productId(), null)).withRel("update-product-status"));

        try {
            ProductStatus currentStatus = ProductStatus.valueOf(response.status().toUpperCase());
            if (currentStatus == ProductStatus.DRAFT) {
                entity.add(linkTo(methodOn(ProductController.class).publish(response.productId(), null)).withRel("publish-product"));
                entity.add(linkTo(methodOn(ProductController.class).deleteProduct(response.productId())).withRel("delete-product"));
            } else if (currentStatus == ProductStatus.ACTIVE) {
                entity.add(linkTo(methodOn(ProductController.class).suspend(response.productId(), null)).withRel("suspend-product"));
                entity.add(linkTo(methodOn(ProductController.class).deleteProduct(response.productId())).withRel("delete-product"));
            } else if (currentStatus == ProductStatus.SUSPENDED || currentStatus == ProductStatus.ARCHIVED) {
                entity.add(linkTo(methodOn(ProductController.class).restore(response.productId(), null)).withRel("restore-product"));
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            // Invalid or missing status, safely ignore adding conditional links
        }

        return entity;
    }
}
