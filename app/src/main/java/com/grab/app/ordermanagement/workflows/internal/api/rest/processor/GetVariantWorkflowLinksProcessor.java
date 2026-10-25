package com.grab.app.ordermanagement.workflows.internal.api.rest.processor;

import com.catalog.domain.valueobject.ProductVariantStatus;
import com.grab.app.productmerchandising.catalog.api.representation.GetVariantResponse;
import com.grab.app.ordermanagement.workflows.api.WorkflowApiLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class GetVariantWorkflowLinksProcessor implements RepresentationModelProcessor<EntityModel<GetVariantResponse>> {
    @Override
    public EntityModel<GetVariantResponse> process(EntityModel<GetVariantResponse> model) {
        GetVariantResponse response = model.getContent();
        if (response == null || response.status() == null) return model;
        try {
            if (ProductVariantStatus.valueOf(response.status().toUpperCase()) != ProductVariantStatus.DELETED) {
                if (!model.hasLink("update-product-variant")) model.add(WorkflowApiLinks.updateProductVariantLink());
            }
        } catch (IllegalArgumentException ignored) {
        }
        return model;
    }
}
