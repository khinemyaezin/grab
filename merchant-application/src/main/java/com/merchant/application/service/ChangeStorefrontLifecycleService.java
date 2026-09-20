package com.merchant.application.service;

import com.merchant.application.util.StorefrontOwnership;

import com.merchant.application.port.inbound.ChangeStorefrontLifecycleUseCase;

import com.grab.framework.id.Id;
import com.merchant.application.model.write.ChangeStorefrontLifecycleCommand;
import com.merchant.application.model.write.StorefrontResult;
import com.merchant.application.exception.MerchantServiceError;
import com.merchant.application.exception.MerchantServiceException;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.aggregate.Storefront;
import com.merchant.domain.port.outbound.MerchantAccountRepository;
import com.merchant.domain.port.outbound.StorefrontRepository;
import com.merchant.domain.service.StorefrontProvisioningService;
import com.merchant.domain.valueobject.LifecycleReason;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class ChangeStorefrontLifecycleService implements ChangeStorefrontLifecycleUseCase {

    private final MerchantAccountRepository merchants;
    private final StorefrontRepository storefronts;
    private final StorefrontProvisioningService provisioningService;
    public StorefrontResult execute(ChangeStorefrontLifecycleCommand command) {
        MerchantAccount merchant = merchants.findById(command.merchantId())
                .orElseThrow(() -> merchantNotFound(command.merchantId()));
        Storefront storefront = StorefrontOwnership.requireOwned(storefronts, command.storefrontId(), command.merchantId());
        Instant now = Instant.now();

        switch (command.action()) {
            case ACTIVATE -> {
                provisioningService.requireOperational(merchant);
                storefront.activate(now);
            }
            case SUSPEND -> storefront.suspend(reason(command), now);
            case REACTIVATE -> {
                provisioningService.requireOperational(merchant);
                storefront.reactivate(now);
            }
            case CLOSE -> storefront.close(reason(command), now);
        }
        return StorefrontResult.from(storefronts.save(storefront));
    }

    private LifecycleReason reason(ChangeStorefrontLifecycleCommand command) {
        return new LifecycleReason(command.reason());
    }

    private MerchantServiceException merchantNotFound(Id merchantId) {
        return new MerchantServiceException(
                new MerchantServiceError.MerchantNotFound(merchantId.getValue()),
                "Merchant account not found"
        );
    }
}
