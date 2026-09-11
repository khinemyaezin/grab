package com.grab.store.catalog.internal.api.rest.assembler;

import com.catalog.domain.valueobject.ProductVariantStatus;
import com.grab.store.catalog.internal.api.rest.controller.ProductController;
import com.grab.store.catalog.internal.api.rest.dto.response.GetVariantResponse;
import com.grab.store.workflows.api.WorkflowApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class GetVariantModelAssembler
        implements RepresentationModelAssembler<GetVariantResponse, EntityModel<GetVariantResponse>> {

    @Override
    public EntityModel<GetVariantResponse> toModel(GetVariantResponse response) {
        EntityModel<GetVariantResponse> entity = EntityModel.of(response);

        entity.add(linkTo(methodOn(ProductController.class)
                .getVariant(response.productId(), response.variantId())).withSelfRel());
        entity.add(linkTo(methodOn(ProductController.class)
                .getProduct(response.productId())).withRel("get-product"));
        entity.add(linkTo(methodOn(ProductController.class)
                .searchProductVariants(null, null, null)).withRel("search-product-variants"));

        try {
            ProductVariantStatus status = ProductVariantStatus.valueOf(response.status().toUpperCase());
            if (status == ProductVariantStatus.DELETED) {
                entity.add(linkTo(methodOn(ProductController.class)
                        .restoreVariant(response.productId(), response.variantId())).withRel("restore-variant"));
            } else {
                entity.add(linkTo(methodOn(ProductController.class)
                        .updateVariant(response.productId(), response.variantId(), null)).withRel("update-variant"));
                entity.add(WorkflowApiLinks.updateProductVariantLink());
                entity.add(linkTo(methodOn(ProductController.class)
                        .deleteVariant(response.productId(), response.variantId())).withRel("delete-variant"));
            }
        } catch (IllegalArgumentException | NullPointerException ignored) {
            // Invalid or missing status, safely ignore adding conditional links
        }

        return entity;
    }
}
