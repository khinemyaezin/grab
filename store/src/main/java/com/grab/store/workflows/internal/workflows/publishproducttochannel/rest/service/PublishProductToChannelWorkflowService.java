package com.grab.store.workflows.internal.workflows.publishproducttochannel.rest.service;

import com.grab.framework.workflow.WorkflowEngine;
import com.grab.framework.workflow.WorkflowInstance;
import com.grab.framework.workflow.WorkflowProcess;
import com.grab.framework.workflow.WorkflowStore;
import com.grab.framework.workflow.support.WorkflowPayloadCodec;
import com.grab.store.shared.exception.SharedErrors;
import com.grab.store.workflows.internal.workflows.createsellableproduct.rest.service.WorkflowSellerAccessResolver;
import com.grab.store.workflows.internal.workflows.publishproducttochannel.PublishProductToChannelContext;
import com.grab.store.workflows.internal.workflows.publishproducttochannel.rest.dto.request.PublishProductToChannelRequest;
import com.grab.store.workflows.internal.workflows.publishproducttochannel.rest.dto.response.PublishProductToChannelResponse;
import com.grab.store.workflows.internal.workflows.publishproducttochannel.rest.mapper.PublishProductToChannelRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PublishProductToChannelWorkflowService {

    private final WorkflowEngine workflowEngine;
    private final WorkflowProcess<PublishProductToChannelContext> publishProductToChannel;
    private final WorkflowStore workflowStore;
    private final WorkflowPayloadCodec payloadCodec;
    private final PublishProductToChannelRequestMapper mapper;

    public PublishProductToChannelResponse start(
            PublishProductToChannelRequest request,
            WorkflowSellerAccessResolver.WorkflowAccess access
    ) {
        PublishProductToChannelContext context = mapper.toContext(
                request,
                access.merchantId(),
                access.actorId(),
                access.scopeKey(),
                access.scopeId()
        );
        WorkflowInstance instance = workflowEngine.start(
                publishProductToChannel.create(),
                context,
                request.idempotencyKey()
        );
        PublishProductToChannelContext savedContext = readContext(instance).orElse(context);
        return mapper.toResponse(instance, savedContext);
    }

    public PublishProductToChannelResponse get(String workflowId) {
        WorkflowInstance instance = workflowStore.findById(workflowId)
                .orElseThrow(() -> SharedErrors.workflowNotFound(workflowId));
        PublishProductToChannelContext context = readContext(instance).orElse(null);
        return mapper.toResponse(instance, context);
    }

    private Optional<PublishProductToChannelContext> readContext(WorkflowInstance instance) {
        return instance.contextJson()
                .map(json -> payloadCodec.readTyped(json, PublishProductToChannelContext.class));
    }
}
