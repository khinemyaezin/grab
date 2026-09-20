package com.merchant.application.service;

import com.merchant.application.util.StorefrontOwnership;

import com.merchant.application.port.inbound.UpdateStorefrontProfileUseCase;

import com.grab.framework.id.Id;
import com.merchant.application.model.write.StorefrontResult;
import com.merchant.application.model.write.UpdateStorefrontProfileCommand;
import com.merchant.application.exception.MerchantServiceError;
import com.merchant.application.exception.MerchantServiceException;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.aggregate.Storefront;
import com.merchant.domain.port.outbound.MerchantAccountRepository;
import com.merchant.domain.port.outbound.StorefrontRepository;
import com.merchant.domain.service.StorefrontSlugPolicy;
import com.merchant.domain.valueobject.StorefrontName;
import com.merchant.domain.valueobject.StorefrontSlug;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class UpdateStorefrontProfileService implements UpdateStorefrontProfileUseCase {

    private final MerchantAccountRepository merchants;
    private final StorefrontRepository storefronts;
    private final StorefrontSlugPolicy slugPolicy;
    public StorefrontResult execute(UpdateStorefrontProfileCommand command) {
        merchants.findById(command.merchantId())
                .orElseThrow(() -> merchantNotFound(command.merchantId()));
        Storefront storefront = StorefrontOwnership.requireOwned(storefronts, command.storefrontId(), command.merchantId());

        StorefrontName name = new StorefrontName(command.name());
        StorefrontSlug slug = command.slug() == null || command.slug().isBlank()
                ? storefront.getSlug()
                : new StorefrontSlug(command.slug());
        slugPolicy.requireAvailable(slug, storefront.getId());
        storefront.rename(name, slug, Instant.now());
        return StorefrontResult.from(storefronts.save(storefront));
    }

    private MerchantServiceException merchantNotFound(Id merchantId) {
        return new MerchantServiceException(
                new MerchantServiceError.MerchantNotFound(merchantId.getValue()),
                "Merchant account not found"
        );
    }
}
