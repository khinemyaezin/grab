package com.catalog.application.service;

import com.catalog.application.port.inbound.UpsertMerchantAvailabilityUseCase;

import com.catalog.domain.port.outbound.MerchantAvailabilityRepository;
import com.catalog.application.model.write.UpsertMerchantAvailabilityCommand;

@lombok.RequiredArgsConstructor
public class UpsertMerchantAvailabilityService implements UpsertMerchantAvailabilityUseCase {

    private final MerchantAvailabilityRepository merchantAvailabilityRepository;

        public Void execute(UpsertMerchantAvailabilityCommand command) {
        merchantAvailabilityRepository.upsert(
                command.merchantId(),
                command.status(),
                command.merchantType()
        );
        return null;
    }
}
