package com.grab.app.platform.seller.port.rest;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateSellableProductRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateProductVariantRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateSellableProductRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CreateSellableProductResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateProductVariantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.UpdateSellableProductResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.WorkflowExecutionResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;

/**
 * Public REST Contract for Cross-Domain Sellable Product Saga Workflows.
 */
public interface SellerWorkflowResource {

    ResponseEntity<EntityModel<CreateSellableProductResponse>> startCreateSellableProduct(CreateSellableProductRequest request, String merchantId);

    ResponseEntity<EntityModel<CreateSellableProductResponse>> getCreateSellableProduct(String workflowId);

    ResponseEntity<EntityModel<UpdateSellableProductResponse>> startUpdateSellableProduct(UpdateSellableProductRequest request, String merchantId);

    ResponseEntity<EntityModel<UpdateSellableProductResponse>> getUpdateSellableProduct(String workflowId);

    ResponseEntity<EntityModel<UpdateProductVariantResponse>> startUpdateProductVariant(UpdateProductVariantRequest request, String merchantId);

    ResponseEntity<EntityModel<UpdateProductVariantResponse>> getUpdateProductVariant(String workflowId);

    ResponseEntity<EntityModel<WorkflowExecutionResponse>> getWorkflowStatus(String workflowId);

    ResponseEntity<CollectionModel<EntityModel<WorkflowExecutionResponse>>> listRecentWorkflows(int page, int size);
}
