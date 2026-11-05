package com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.controller.SellerSellableProductWorkflowController;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.WorkflowExecutionResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class WorkflowExecutionModelAssembler implements RepresentationModelAssembler<WorkflowExecutionResponse, EntityModel<WorkflowExecutionResponse>> {

    @Override
    public EntityModel<WorkflowExecutionResponse> toModel(WorkflowExecutionResponse workflow) {
        if (workflow == null) {
            return null;
        }
        EntityModel<WorkflowExecutionResponse> model = EntityModel.of(workflow);
        if (workflow.workflowId() != null) {
            model.add(linkTo(methodOn(SellerSellableProductWorkflowController.class).getWorkflowStatus(workflow.workflowId())).withSelfRel());
        }
        model.add(linkTo(methodOn(SellerSellableProductWorkflowController.class).listRecentWorkflows(0, 20)).withRel("list-recent-workflows"));
        return model;
    }
}

