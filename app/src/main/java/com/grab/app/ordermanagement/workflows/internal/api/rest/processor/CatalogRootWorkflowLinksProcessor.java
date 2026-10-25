package com.grab.app.ordermanagement.workflows.internal.api.rest.processor;

import com.grab.app.productmerchandising.catalog.api.representation.CatalogRootModel;
import com.grab.app.ordermanagement.workflows.api.WorkflowApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class CatalogRootWorkflowLinksProcessor implements RepresentationModelProcessor<CatalogRootModel> {
    @Override
    public CatalogRootModel process(CatalogRootModel model) {
        if (!model.hasLink("create-sellable-product")) model.add(WorkflowApiLinks.createSellableProductLink());
        if (!model.hasLink("update-sellable-product")) model.add(WorkflowApiLinks.updateSellableProductLink());
        if (!model.hasLink("update-product-variant")) model.add(WorkflowApiLinks.updateProductVariantLink());
        return model;
    }
}
