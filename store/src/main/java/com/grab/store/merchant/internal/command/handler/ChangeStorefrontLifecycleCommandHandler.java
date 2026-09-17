package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.Id;
import com.grab.store.merchant.internal.command.ChangeStorefrontLifecycleCommand;
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
import com.merchant.domain.valueobject.LifecycleReason;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@MerchantEnabled
@RequiredArgsConstructor
public class ChangeStorefrontLifecycleCommandHandler
        implements CommandHandler<ChangeStorefrontLifecycleCommand, StorefrontResult> {

    private final MerchantAccountRepository merchants;
    private final StorefrontRepository storefronts;
    private final StorefrontProvisioningService provisioningService;

    @Override
    @MerchantTransactional
    public StorefrontResult handle(ChangeStorefrontLifecycleCommand command) {
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

    @Override
    public Class<ChangeStorefrontLifecycleCommand> getCommandType() {
        return ChangeStorefrontLifecycleCommand.class;
    }
}
