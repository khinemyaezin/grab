package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.ProductSummaryResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerProductApiLinks;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductSummaryModelAssembler implements RepresentationModelAssembler<ProductSummaryResponse, EntityModel<ProductSummaryResponse>> {

    @Override
    public EntityModel<ProductSummaryResponse> toModel(ProductSummaryResponse summary) {
        if (summary == null) {
            return null;
        }
        EntityModel<ProductSummaryResponse> model = EntityModel.of(summary);
        if (summary.productId() != null) {
            model.add(SellerProductApiLinks.getProduct(summary.productId()));
        }
        return model;
    }

    public PagedModel<EntityModel<ProductSummaryResponse>> toPageModel(
            Page<ProductSummaryResponse> products, String query, int page, int size) {
        List<EntityModel<ProductSummaryResponse>> items = products.getContent().stream()
                .map(this::toModel)
                .toList();
        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(size, page, products.getTotalElements());
        PagedModel<EntityModel<ProductSummaryResponse>> model = PagedModel.of(items, metadata);
        model.add(SellerProductApiLinks.searchProducts(query, page, size).expand().withSelfRel());
        model.add(SellerProductApiLinks.listProducts());
        model.add(SellerProductApiLinks.searchProducts(null, 0, size));
        model.add(SellerProductApiLinks.createProduct());
        if (products.hasPrevious()) {
            model.add(SellerProductApiLinks.searchProducts(query, page - 1, size).expand()
                    .withRel(IanaLinkRelations.PREV));
        }
        if (products.hasNext()) {
            model.add(SellerProductApiLinks.searchProducts(query, page + 1, size).expand()
                    .withRel(IanaLinkRelations.NEXT));
        }
        return model;
    }
}
