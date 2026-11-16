package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.WorkflowExecutionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.hateoas.SellerWorkflowApiLinks;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkflowExecutionModelAssembler implements RepresentationModelAssembler<WorkflowExecutionResponse, EntityModel<WorkflowExecutionResponse>> {

    @Override
    public EntityModel<WorkflowExecutionResponse> toModel(WorkflowExecutionResponse workflow) {
        if (workflow == null) {
            return null;
        }
        EntityModel<WorkflowExecutionResponse> model = EntityModel.of(workflow);
        if (workflow.workflowId() != null) {
            model.add(SellerWorkflowApiLinks.getWorkflowStatus(workflow.workflowId()).withSelfRel());
        }
        model.add(SellerWorkflowApiLinks.listRecentWorkflows(0, 20));
        return model;
    }

    public CollectionModel<EntityModel<WorkflowExecutionResponse>> listRecentWorkflowsModel(List<WorkflowExecutionResponse> responses, int page, int size) {
        List<EntityModel<WorkflowExecutionResponse>> items = responses.stream()
                .map(this::toModel)
                .toList();
        Link collection = SellerWorkflowApiLinks.listRecentWorkflows(page, size).expand();
        CollectionModel<EntityModel<WorkflowExecutionResponse>> model = CollectionModel.of(items, collection.withSelfRel(), collection);
        return model;
    }
}
