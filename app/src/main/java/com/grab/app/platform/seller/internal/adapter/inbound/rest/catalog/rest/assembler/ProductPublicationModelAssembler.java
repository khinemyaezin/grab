package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.ProductController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response.ProductPublicationResponse;
import com.grab.app.workflows.api.WorkflowApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProductPublicationModelAssembler
        implements RepresentationModelAssembler<ProductPublicationResponse, EntityModel<ProductPublicationResponse>> {

    @Override
    public EntityModel<ProductPublicationResponse> toModel(ProductPublicationResponse response) {
        EntityModel<ProductPublicationResponse> entity = EntityModel.of(response);
        entity.add(linkTo(methodOn(ProductController.class).getProduct(response.productId())).withRel("get-product"));
        entity.add(linkTo(methodOn(ProductController.class).unpublishFromChannel(response.productId(), null))
                .withRel("unpublish-product-from-channel"));
        entity.add(WorkflowApiLinks.updateSellableProductLink());
        return entity;
    }
}
