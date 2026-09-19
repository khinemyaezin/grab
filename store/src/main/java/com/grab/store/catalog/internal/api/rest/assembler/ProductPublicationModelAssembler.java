package com.grab.store.catalog.internal.api.rest.assembler;

import com.grab.store.catalog.internal.api.rest.controller.ProductController;
import com.grab.store.catalog.internal.api.rest.dto.response.ProductPublicationResponse;
import com.grab.store.workflows.api.WorkflowApiLinks;
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
