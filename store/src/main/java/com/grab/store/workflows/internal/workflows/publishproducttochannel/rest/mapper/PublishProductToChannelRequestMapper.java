package com.grab.store.workflows.internal.workflows.publishproducttochannel.rest.mapper;

import com.grab.framework.workflow.WorkflowInstance;
import com.grab.store.workflows.internal.workflows.publishproducttochannel.PublishProductToChannelContext;
import com.grab.store.workflows.internal.workflows.publishproducttochannel.rest.dto.request.PublishProductToChannelRequest;
import com.grab.store.workflows.internal.workflows.publishproducttochannel.rest.dto.response.PublishProductToChannelResponse;
import org.springframework.stereotype.Component;

@Component
public class PublishProductToChannelRequestMapper {

    public PublishProductToChannelContext toContext(
            PublishProductToChannelRequest request,
            String merchantId,
            String createdBy,
            String scopeKey,
            String scopeId
    ) {
        return PublishProductToChannelContext.createContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                request.productId(),
                request.salesChannelId()
        );
    }

    public PublishProductToChannelResponse toResponse(
            WorkflowInstance instance,
            PublishProductToChannelContext context
    ) {
        return new PublishProductToChannelResponse(
                instance.id(),
                instance.status().name(),
                instance.currentStep().orElse(null),
                context == null ? null : context.productId(),
                context == null ? null : context.salesChannelId(),
                context == null || !context.stockPathChecked() ? null : context.missingRoute(),
                instance.errorMessage().orElse(null),
                instance.updatedAt()
        );
    }
}
