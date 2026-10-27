package com.grab.app.workflows.port;

/**
 * Public workflow capability exposed to assemblies that update product variants.
 */
public interface UpdateProductVariantWorkflowPort {

    StartResult start(StartRequest request);

    StatusResult get(String workflowId);

    record StartRequest(String merchantId, String idempotencyKey, String payload) {
    }

    record StartResult(String workflowId, String status) {
    }

    record StatusResult(String workflowId, String status, String payload) {
    }
}
