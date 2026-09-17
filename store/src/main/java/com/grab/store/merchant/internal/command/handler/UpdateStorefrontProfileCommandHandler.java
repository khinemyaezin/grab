package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.Id;
import com.grab.store.merchant.internal.command.StorefrontResult;
import com.grab.store.merchant.internal.command.UpdateStorefrontProfileCommand;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.merchant.internal.config.MerchantTransactional;
import com.grab.store.merchant.internal.exception.MerchantServiceError;
import com.grab.store.merchant.internal.exception.MerchantServiceException;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.aggregate.Storefront;
import com.merchant.domain.repository.MerchantAccountRepository;
import com.merchant.domain.repository.StorefrontRepository;
import com.merchant.domain.service.StorefrontSlugPolicy;
import com.merchant.domain.valueobject.StorefrontName;
import com.merchant.domain.valueobject.StorefrontSlug;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@MerchantEnabled
@RequiredArgsConstructor
public class UpdateStorefrontProfileCommandHandler
        implements CommandHandler<UpdateStorefrontProfileCommand, StorefrontResult> {

    private final MerchantAccountRepository merchants;
    private final StorefrontRepository storefronts;
    private final StorefrontSlugPolicy slugPolicy;

    @Override
    @MerchantTransactional
    public StorefrontResult handle(UpdateStorefrontProfileCommand command) {
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

    @Override
    public Class<UpdateStorefrontProfileCommand> getCommandType() {
        return UpdateStorefrontProfileCommand.class;
    }
}
