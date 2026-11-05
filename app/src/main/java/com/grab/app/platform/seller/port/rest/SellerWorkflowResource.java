package com.grab.app.platform.seller.port.rest;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateSellableProductWorkflowRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateProductVariantWorkflowRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateSellableProductWorkflowRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.WorkflowExecutionResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;

/**
 * Public REST Contract for Cross-Domain Sellable Product Saga Workflows.
 */
public interface SellerWorkflowResource {

    ResponseEntity<EntityModel<WorkflowExecutionResponse>> startCreateSellableProduct(CreateSellableProductWorkflowRequest request, String merchantId);

    ResponseEntity<EntityModel<WorkflowExecutionResponse>> startUpdateSellableProduct(String productId, UpdateSellableProductWorkflowRequest request, String merchantId);

    ResponseEntity<EntityModel<WorkflowExecutionResponse>> startUpdateProductVariant(String productId, String variantId, UpdateProductVariantWorkflowRequest request, String merchantId);

    ResponseEntity<EntityModel<WorkflowExecutionResponse>> getWorkflowStatus(String workflowId);

    ResponseEntity<CollectionModel<EntityModel<WorkflowExecutionResponse>>> listRecentWorkflows(int page, int size);
}

