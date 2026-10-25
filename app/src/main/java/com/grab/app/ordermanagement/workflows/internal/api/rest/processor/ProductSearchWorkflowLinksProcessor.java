package com.grab.app.ordermanagement.workflows.internal.api.rest.processor;

import com.catalog.domain.valueobject.ProductStatus;
import com.grab.app.productmerchandising.catalog.api.representation.ProductSearchResponse;
import com.grab.app.ordermanagement.workflows.api.WorkflowApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class ProductSearchWorkflowLinksProcessor implements RepresentationModelProcessor<EntityModel<ProductSearchResponse>> {
    @Override
    public EntityModel<ProductSearchResponse> process(EntityModel<ProductSearchResponse> model) {
        ProductSearchResponse response = model.getContent();
        if (response == null || response.status() == null) return model;
        try {
            if (ProductStatus.valueOf(response.status().toUpperCase()) == ProductStatus.ACTIVE) {
                if (!model.hasLink("update-sellable-product")) model.add(WorkflowApiLinks.updateSellableProductLink());
            }
        } catch (IllegalArgumentException ignored) {
        }
        return model;
    }
}
