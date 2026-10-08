package com.grab.store.merchant.internal.api.adapter;

import com.grab.framework.id.IdGenerator;
import com.grab.store.merchant.internal.config.MerchantReadTransactional;
import com.grab.store.shared.security.StorefrontOwnerQuery;
import com.merchant.application.exception.MerchantServiceException;
import com.merchant.application.model.read.GetStorefrontQuery;
import com.merchant.application.port.inbound.GetStorefrontUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MerchantStorefrontOwnerQueryAdapter implements StorefrontOwnerQuery {
    private final GetStorefrontUseCase getStorefrontUseCase;
    private final IdGenerator idGenerator;

    @Override
    @MerchantReadTransactional
    public boolean belongsToMerchant(String storefrontId, String merchantId) {
        var storefrontIdentifier = idGenerator.convertIdFrom(storefrontId);
        var merchantIdentifier = idGenerator.convertIdFrom(merchantId);
        var query = new GetStorefrontQuery(storefrontIdentifier, merchantIdentifier);
        try {
            var result = getStorefrontUseCase.execute(query);
            return result != null;
        } catch (MerchantServiceException exception) {
            return false;
        }
    }
}
