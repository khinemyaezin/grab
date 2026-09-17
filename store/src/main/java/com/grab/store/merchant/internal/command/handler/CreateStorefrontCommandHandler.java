package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.merchant.internal.command.CreateStorefrontCommand;
import com.grab.store.merchant.internal.command.StorefrontResult;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.merchant.internal.config.MerchantTransactional;
import com.grab.store.merchant.internal.exception.MerchantServiceError;
import com.grab.store.merchant.internal.exception.MerchantServiceException;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.aggregate.Storefront;
import com.merchant.domain.repository.MerchantAccountRepository;
import com.merchant.domain.repository.StorefrontRepository;
import com.merchant.domain.service.StorefrontProvisioningService;
import com.merchant.domain.service.StorefrontSlugPolicy;
import com.merchant.domain.valueobject.StorefrontName;
import com.merchant.domain.valueobject.StorefrontSlug;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@MerchantEnabled
@RequiredArgsConstructor
public class CreateStorefrontCommandHandler
        implements CommandHandler<CreateStorefrontCommand, StorefrontResult> {

    private final MerchantAccountRepository merchants;
    private final StorefrontRepository storefronts;
    private final StorefrontProvisioningService provisioningService;
    private final StorefrontSlugPolicy slugPolicy;
    private final IdGenerator ids;

    @Override
    @MerchantTransactional
    public StorefrontResult handle(CreateStorefrontCommand command) {
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

    @Override
    public Class<CreateStorefrontCommand> getCommandType() {
        return CreateStorefrontCommand.class;
    }
}
