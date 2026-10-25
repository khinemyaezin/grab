package com.grab.app.ordermanagement.workflows.internal.api.rest.processor;

import com.catalog.domain.valueobject.ProductStatus;
import com.grab.app.productmerchandising.catalog.api.representation.GetProductResponse;
import com.grab.app.ordermanagement.workflows.api.WorkflowApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class GetProductWorkflowLinksProcessor implements RepresentationModelProcessor<EntityModel<GetProductResponse>> {
    @Override
    public EntityModel<GetProductResponse> process(EntityModel<GetProductResponse> model) {
        GetProductResponse response = model.getContent();
        if (response == null || response.status() == null) return model;
        try {
            ProductStatus status = ProductStatus.valueOf(response.status().toUpperCase());
            if (status == ProductStatus.DRAFT || status == ProductStatus.ACTIVE) {
                if (!model.hasLink("update-sellable-product")) model.add(WorkflowApiLinks.updateSellableProductLink());
            }
        } catch (IllegalArgumentException ignored) {
        }
        return model;
    }
}
