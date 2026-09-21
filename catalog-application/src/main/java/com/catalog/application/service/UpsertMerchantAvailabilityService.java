package com.catalog.application.service;

import com.catalog.application.port.inbound.UpsertMerchantAvailabilityUseCase;

import com.catalog.application.port.outbound.MerchantAvailabilityPort;
import com.catalog.application.model.write.UpsertMerchantAvailabilityCommand;

@lombok.RequiredArgsConstructor
public class UpsertMerchantAvailabilityService implements UpsertMerchantAvailabilityUseCase {

    private final MerchantAvailabilityPort merchantAvailabilityPort;

        public Void execute(UpsertMerchantAvailabilityCommand command) {
        merchantAvailabilityPort.upsert(
                command.merchantId(),
                command.status(),
                command.merchantType()
        );
        return null;
    }
}
