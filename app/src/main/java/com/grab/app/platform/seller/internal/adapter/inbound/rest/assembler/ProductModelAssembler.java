package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.ProductResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerCategoryApiLinks;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerProductApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class ProductModelAssembler implements RepresentationModelAssembler<ProductResponse, EntityModel<ProductResponse>> {

    @Override
    public EntityModel<ProductResponse> toModel(ProductResponse response) {
        return assemble(response, true);
    }

    public EntityModel<ProductResponse> toCommandModel(ProductResponse response) {
        return assemble(response, false);
    }

    private EntityModel<ProductResponse> assemble(ProductResponse response, boolean canonicalRead) {
        if (response == null) {
            return null;
        }
        EntityModel<ProductResponse> model = EntityModel.of(response);
        if (response.id() != null) {
            Link retrieval = SellerProductApiLinks.getProduct(response.id());
            model.add(canonicalRead ? retrieval.withSelfRel() : retrieval);
            model.add(SellerProductApiLinks.updateProduct(response.id()));
            model.add(SellerProductApiLinks.deleteProduct(response.id()));
            model.add(SellerProductApiLinks.updateProductStatus(response.id()));
        }
        if (response.slug() != null) {
            model.add(SellerProductApiLinks.getProductBySlug(response.slug()));
        }
        if (response.categoryId() != null) {
            model.add(SellerCategoryApiLinks.getCategory(response.categoryId()));
        }
        model.add(SellerProductApiLinks.listProducts());
        model.add(SellerProductApiLinks.searchProducts(null, 0, 20));
        return model;
    }
}
