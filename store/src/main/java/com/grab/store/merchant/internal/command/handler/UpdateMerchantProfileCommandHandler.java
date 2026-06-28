package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.Id;
import com.grab.store.merchant.internal.command.MerchantAccountResult;
import com.grab.store.merchant.internal.command.UpdateMerchantProfileCommand;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.merchant.internal.config.MerchantTransactional;
import com.grab.store.merchant.internal.exception.MerchantServiceError;
import com.grab.store.merchant.internal.exception.MerchantServiceException;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.repository.MerchantAccountRepository;
import com.merchant.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@MerchantEnabled
@RequiredArgsConstructor
public class UpdateMerchantProfileCommandHandler
        implements CommandHandler<UpdateMerchantProfileCommand, MerchantAccountResult> {

    private final MerchantAccountRepository merchants;

    @Override
    @MerchantTransactional
    public MerchantAccountResult handle(UpdateMerchantProfileCommand command) {
        MerchantAccount merchant = find(command.merchantId());
        merchant.requireApplicant(command.applicantUserId());

        MerchantName name = new MerchantName(command.legalName(), command.displayName());
        BusinessRegistration registration = registration(command);
        ContactInformation contact = new ContactInformation(command.contactEmail(), command.contactPhone());
        RegisteredAddress address = new RegisteredAddress(
                command.addressLine1(), command.addressLine2(), command.addressCity(),
                command.addressRegion(), command.addressPostalCode(), command.addressCountryCode());
        Instant now = Instant.now();
        merchant.updateProfile(
                name,
                registration,
                contact,
                address,
                now
        );
        MerchantAccount saved = merchants.save(merchant);
        return MerchantAccountResult.from(saved);
    }

    private BusinessRegistration registration(UpdateMerchantProfileCommand command) {
        if (isBlank(command.registrationCountryCode()) && isBlank(command.registrationNumber())) return null;
        return new BusinessRegistration(command.registrationCountryCode(), command.registrationNumber());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private MerchantAccount find(Id merchantId) {
        return merchants.findById(merchantId).orElseThrow(() -> notFound(merchantId));
    }

    private MerchantServiceException notFound(Id merchantId) {
        MerchantServiceError error = new MerchantServiceError.MerchantNotFound(merchantId.getValue());
        return new MerchantServiceException(error, "Merchant account not found");
    }

    @Override
    public Class<UpdateMerchantProfileCommand> getCommandType() {
        return UpdateMerchantProfileCommand.class;
    }
}
