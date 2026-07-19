package com.grab.store.catalog.internal.api.rest.assembler;

import com.catalog.domain.valueobject.ProductStatus;
import com.grab.store.catalog.internal.api.rest.controller.ProductController;
import com.grab.store.catalog.internal.api.rest.dto.response.ProductVariantSearchResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProductVariantSummaryModelAssembler
        implements RepresentationModelAssembler<ProductVariantSearchResponse, EntityModel<ProductVariantSearchResponse>> {

    @Override
    public EntityModel<ProductVariantSearchResponse> toModel(ProductVariantSearchResponse response) {
        EntityModel<ProductVariantSearchResponse> entity = EntityModel.of(response);

        entity.add(linkTo(methodOn(ProductController.class).getProduct(response.productId())).withRel("get-product"));
        entity.add(linkTo(methodOn(ProductController.class)
                .updateVariant(response.productId(), response.sku(), null))
                .withRel("update-variant"));

        try {
            ProductStatus currentStatus = ProductStatus.valueOf(response.status().toUpperCase());
            if (currentStatus == ProductStatus.DRAFT) {
                entity.add(linkTo(methodOn(ProductController.class).publish(response.productId(), null))
                        .withRel("publish-product"));
            } else if (currentStatus == ProductStatus.ACTIVE) {
                entity.add(linkTo(methodOn(ProductController.class).suspend(response.productId(), null))
                        .withRel("suspend-product"));
            } else if (currentStatus == ProductStatus.SUSPENDED || currentStatus == ProductStatus.ARCHIVED) {
                entity.add(linkTo(methodOn(ProductController.class).restore(response.productId(), null))
                        .withRel("restore-product"));
            }
        } catch (IllegalArgumentException | NullPointerException ignored) {
            // Invalid or missing status, safely ignore adding conditional links
        }

        return entity;
    }
}
