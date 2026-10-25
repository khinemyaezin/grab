package com.grab.app.productmerchandising.catalog.internal.api.rest.assembler;

import com.catalog.domain.valueobject.ProductStatus;
import com.grab.app.productmerchandising.catalog.internal.api.rest.controller.ProductController;
import com.grab.app.productmerchandising.catalog.internal.api.rest.controller.CategoryController;
import com.grab.app.productmerchandising.catalog.api.representation.GetProductResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class GetProductModelAssembler
        implements RepresentationModelAssembler<GetProductResponse, EntityModel<GetProductResponse>> {

    @Override
    public EntityModel<GetProductResponse> toModel(GetProductResponse response) {
        EntityModel<GetProductResponse> entity = EntityModel.of(response);

        entity.add(linkTo(methodOn(ProductController.class).getProduct(response.id())).withSelfRel());
        entity.add(linkTo(methodOn(ProductController.class).getProductBySlug(response.slug())).withRel("get-product-by-slug"));
        entity.add(linkTo(methodOn(ProductController.class).searchProducts(null, null, null)).withRel("search-products"));
        entity.add(linkTo(methodOn(CategoryController.class).getCategory(response.category().id())).withRel("get-category"));
        entity.add(linkTo(methodOn(ProductController.class).updateProduct(response.id(), null)).withRel("update-product"));
        entity.add(linkTo(methodOn(ProductController.class).updateProductStatus(response.id(), null)).withRel("update-product-status"));
        entity.add(linkTo(methodOn(ProductController.class).createProductMediaUpload(response.id(), null))
                .withRel("create-product-media-upload"));
        entity.add(linkTo(methodOn(ProductController.class).replaceProductMedia(response.id(), null))
                .withRel("replace-product-media"));
        entity.add(linkTo(methodOn(ProductController.class).replaceProductDescriptions(response.id(), null))
                .withRel("replace-product-descriptions"));

        try {
            ProductStatus currentStatus = ProductStatus.valueOf(response.status().toUpperCase());
            if (currentStatus == ProductStatus.DRAFT) {
                entity.add(linkTo(methodOn(ProductController.class).publish(response.id(), null)).withRel("publish-product"));
                entity.add(linkTo(methodOn(ProductController.class).deleteProduct(response.id())).withRel("delete-product"));
            } else if (currentStatus == ProductStatus.ACTIVE) {
                entity.add(linkTo(methodOn(ProductController.class).suspend(response.id(), null)).withRel("suspend-product"));
                entity.add(linkTo(methodOn(ProductController.class).deleteProduct(response.id())).withRel("delete-product"));
            } else if (currentStatus == ProductStatus.SUSPENDED || currentStatus == ProductStatus.ARCHIVED) {
                entity.add(linkTo(methodOn(ProductController.class).restore(response.id(), null)).withRel("restore-product"));
            }
        } catch (IllegalArgumentException | NullPointerException ignored) {
        }

        boolean hasPublications = response.variants() != null
                && response.variants().stream()
                .anyMatch(variant -> variant.publications() != null && !variant.publications().isEmpty());
        if (hasPublications) {
            entity.add(linkTo(methodOn(ProductController.class).unpublishFromChannel(response.id(), null))
                    .withRel("unpublish-product-from-channel"));
        }
        return entity;
    }
}
