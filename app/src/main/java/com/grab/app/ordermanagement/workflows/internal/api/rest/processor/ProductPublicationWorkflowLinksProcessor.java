package com.grab.app.ordermanagement.workflows.internal.api.rest.processor;

import com.grab.app.productmerchandising.catalog.api.representation.ProductPublicationResponse;
import com.grab.app.ordermanagement.workflows.api.WorkflowApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class ProductPublicationWorkflowLinksProcessor implements RepresentationModelProcessor<EntityModel<ProductPublicationResponse>> {
    @Override
    public EntityModel<ProductPublicationResponse> process(EntityModel<ProductPublicationResponse> model) {
        if (!model.hasLink("update-sellable-product")) model.add(WorkflowApiLinks.updateSellableProductLink());
        return model;
    }
}
