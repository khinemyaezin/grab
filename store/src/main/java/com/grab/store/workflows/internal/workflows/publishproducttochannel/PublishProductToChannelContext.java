package com.grab.store.workflows.internal.workflows.publishproducttochannel;

public record PublishProductToChannelContext(
        String merchantId,
        String createdBy,
        String scopeKey,
        String scopeId,
        String productId,
        String salesChannelId,
        boolean channelAsserted,
        boolean productAsserted,
        boolean stockPathChecked,
        boolean missingRoute,
        boolean publicationWritten,
        boolean publicationCompensated
) {

    public static PublishProductToChannelContext createContext(
            String merchantId,
            String createdBy,
            String scopeKey,
            String scopeId,
            String productId,
            String salesChannelId
    ) {
        return new PublishProductToChannelContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                productId,
                salesChannelId,
                false,
                false,
                false,
                false,
                false,
                false
        );
    }

    public PublishProductToChannelContext withChannelAsserted() {
        return new PublishProductToChannelContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                productId,
                salesChannelId,
                true,
                productAsserted,
                stockPathChecked,
                missingRoute,
                publicationWritten,
                publicationCompensated
        );
    }

    public PublishProductToChannelContext withProductAsserted() {
        return new PublishProductToChannelContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                productId,
                salesChannelId,
                channelAsserted,
                true,
                stockPathChecked,
                missingRoute,
                publicationWritten,
                publicationCompensated
        );
    }

    public PublishProductToChannelContext withStockPathChecked(boolean missingRouteFlag) {
        return new PublishProductToChannelContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                productId,
                salesChannelId,
                channelAsserted,
                productAsserted,
                true,
                missingRouteFlag,
                publicationWritten,
                publicationCompensated
        );
    }

    public PublishProductToChannelContext withPublicationWritten() {
        return new PublishProductToChannelContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                productId,
                salesChannelId,
                channelAsserted,
                productAsserted,
                stockPathChecked,
                missingRoute,
                true,
                publicationCompensated
        );
    }

    public PublishProductToChannelContext withPublicationCompensated() {
        return new PublishProductToChannelContext(
                merchantId,
                createdBy,
                scopeKey,
                scopeId,
                productId,
                salesChannelId,
                channelAsserted,
                productAsserted,
                stockPathChecked,
                missingRoute,
                publicationWritten,
                true
        );
    }
}
