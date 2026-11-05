package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerProductController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.ProductResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProductModelAssembler implements RepresentationModelAssembler<ProductResponse, EntityModel<ProductResponse>> {

    @Override
    public EntityModel<ProductResponse> toModel(ProductResponse response) {
        if (response == null) {
            return null;
        }
        EntityModel<ProductResponse> model = EntityModel.of(response);
        if (response.id() != null) {
            model.add(linkTo(methodOn(SellerProductController.class).getProduct(response.id(), null)).withSelfRel());
            model.add(linkTo(methodOn(SellerProductController.class).updateProduct(response.id(), null, null)).withRel("update-product"));
            model.add(linkTo(methodOn(SellerProductController.class).deleteProduct(response.id(), null)).withRel("delete-product"));
            model.add(linkTo(methodOn(SellerProductController.class).updateProductStatus(response.id(), null, null)).withRel("change-product-status"));
        }
        if (response.slug() != null) {
            model.add(linkTo(methodOn(SellerProductController.class).getProductBySlug(response.slug())).withRel("get-product-by-slug"));
        }
        model.add(linkTo(methodOn(SellerProductController.class).searchProducts(null, null, 0, 20)).withRel("list-products"));
        return model;
    }
}
