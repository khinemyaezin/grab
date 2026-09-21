package com.merchant.application.service;

import com.merchant.application.port.inbound.CreateStorefrontUseCase;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.merchant.application.model.write.CreateStorefrontCommand;
import com.merchant.application.model.write.StorefrontResult;
import com.merchant.application.exception.MerchantServiceError;
import com.merchant.application.exception.MerchantServiceException;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.aggregate.Storefront;
import com.merchant.domain.port.outbound.MerchantAccountRepository;
import com.merchant.domain.port.outbound.StorefrontRepository;
import com.merchant.domain.service.StorefrontProvisioningService;
import com.merchant.domain.service.StorefrontSlugPolicy;
import com.merchant.domain.valueobject.StorefrontName;
import com.merchant.domain.valueobject.StorefrontSlug;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class CreateStorefrontService implements CreateStorefrontUseCase {

    private final MerchantAccountRepository merchants;
    private final StorefrontRepository storefronts;
    private final StorefrontProvisioningService provisioningService;
    private final StorefrontSlugPolicy slugPolicy;
    private final IdGenerator ids;
    public StorefrontResult execute(CreateStorefrontCommand command) {
        MerchantAccount merchant = merchants.findById(command.merchantId())
                .orElseThrow(() -> notFound(command.merchantId()));
        provisioningService.requireOperational(merchant);

        StorefrontSlug slug = new StorefrontSlug(command.slug());
        slugPolicy.requireAvailable(slug, null);

        Storefront storefront = Storefront.createDraft(
                ids.generateId(),
                merchant.getId(),
                new StorefrontName(command.name()),
                slug,
                Instant.now()
        );
        return StorefrontResult.from(storefronts.save(storefront));
    }

    private MerchantServiceException notFound(Id merchantId) {
        return new MerchantServiceException(
                new MerchantServiceError.MerchantNotFound(merchantId.getValue()),
                "Merchant account not found"
        );
    }
}
