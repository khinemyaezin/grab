package com.pricing.application.service;

import com.pricing.application.port.inbound.CreatePriceListUseCase;

import com.grab.framework.id.IdGenerator;
import com.pricing.application.model.write.CreatePriceListCommand;
import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.util.PricingResultMapper;
import com.pricing.domain.aggregate.PriceList;
import com.pricing.domain.enums.PriceListType;
import com.pricing.domain.port.outbound.PriceListRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class CreatePriceListService implements CreatePriceListUseCase {

    private final PriceListRepository priceListRepository;
    private final IdGenerator idGenerator;
    public PriceListResult execute(CreatePriceListCommand command) {
        PriceListType type = command.type() == null || command.type().isBlank()
                ? PriceListType.SALE
                : PriceListType.valueOf(command.type().trim().toUpperCase());
        PriceList priceList = PriceList.create(
                idGenerator.generateId(),
                command.title(),
                command.description(),
                type,
                command.startsAt(),
                command.endsAt(),
                Instant.now()
        );
        PriceList saved = priceListRepository.save(priceList);
        return PricingResultMapper.toPriceListResult(saved);
    }
}
