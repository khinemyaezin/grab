package com.grab.app.platform.seller.port.rest;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreatePublishableKeyRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.ProvisionWebsiteChannelRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.KeyResolutionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PublishableKeyResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SalesChannelProvisionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SalesChannelResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;

/**
 * Public REST Contract for Sales Channels and Publishable API Keys.
 */
public interface SellerSalesChannelResource {

    ResponseEntity<EntityModel<SalesChannelProvisionResponse>> provisionMerchantWebsiteChannel(ProvisionWebsiteChannelRequest request, String merchantId);

    ResponseEntity<EntityModel<SalesChannelResponse>> getChannel(String salesChannelId, String merchantId);

    ResponseEntity<CollectionModel<EntityModel<SalesChannelResponse>>> listChannels(String merchantId, int page, int size);

    /**
     * Public REST Contract for Channel Publishable API Keys and Token Resolution.
     */
    interface PublishableKeys {
        ResponseEntity<EntityModel<PublishableKeyResponse>> createPublishableKey(String channelId, CreatePublishableKeyRequest request);
        ResponseEntity<Void> revokePublishableKey(String channelId, String keyId);
        ResponseEntity<KeyResolutionResponse> resolveKey(String token);
    }
}

