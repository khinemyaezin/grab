package com.grab.store.catalog.internal.api.rest.assembler;

import com.catalog.domain.valueobject.ProductStatus;
import com.grab.store.catalog.internal.api.rest.controller.ProductController;
import com.grab.store.catalog.internal.api.rest.dto.response.ProductSummaryResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProductSummaryModelAssembler
        implements RepresentationModelAssembler<ProductSummaryResponse, EntityModel<ProductSummaryResponse>> {

    @Override
    public EntityModel<ProductSummaryResponse> toModel(ProductSummaryResponse response) {
        EntityModel<ProductSummaryResponse> entity = EntityModel.of(response);

        entity.add(linkTo(methodOn(ProductController.class).updateProduct(response.id(), null)).withRel("update-product"));
        entity.add(linkTo(methodOn(ProductController.class).deleteProduct(response.id())).withRel("delete-product"));
        entity.add(linkTo(methodOn(ProductController.class).updateProductStatus(response.id(), null)).withRel("update-product-status"));

        try {
            ProductStatus currentStatus = ProductStatus.valueOf(response.status().toUpperCase());

            if (currentStatus == ProductStatus.DRAFT) {
                entity.add(linkTo(methodOn(ProductController.class).publish(response.id(), null)).withRel("publish-product"));
            } else if (currentStatus == ProductStatus.ACTIVE) {
                entity.add(linkTo(methodOn(ProductController.class).suspend(response.id(), null)).withRel("suspend-product"));
            } else if (currentStatus == ProductStatus.SUSPENDED) {
                entity.add(linkTo(methodOn(ProductController.class).restore(response.id(), null)).withRel("restore-product"));
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            // Invalid or missing status, safely ignore adding conditional links
        }

        return entity;
    }
}
