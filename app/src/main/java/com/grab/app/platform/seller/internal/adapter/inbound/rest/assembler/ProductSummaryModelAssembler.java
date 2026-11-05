package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerProductController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.ProductSummaryResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProductSummaryModelAssembler implements RepresentationModelAssembler<ProductSummaryResponse, EntityModel<ProductSummaryResponse>> {

    @Override
    public EntityModel<ProductSummaryResponse> toModel(ProductSummaryResponse summary) {
        if (summary == null) {
            return null;
        }
        EntityModel<ProductSummaryResponse> model = EntityModel.of(summary);
        if (summary.productId() != null) {
            model.add(linkTo(methodOn(SellerProductController.class).getProduct(summary.productId(), null)).withRel("get-product"));
        }
        return model;
    }
}
