package com.pricing.application.service;

import com.pricing.application.port.inbound.UpdatePriceListUseCase;

import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.model.write.UpdatePriceListCommand;
import com.pricing.application.exception.PricingServiceError;
import com.pricing.application.exception.PricingServiceException;
import com.pricing.application.util.PricingResultMapper;
import com.pricing.domain.aggregate.PriceList;
import com.pricing.domain.enums.PriceListStatus;
import com.pricing.domain.enums.PriceListType;
import com.pricing.domain.port.outbound.PriceListRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class UpdatePriceListService implements UpdatePriceListUseCase {

    private final PriceListRepository priceListRepository;
    public PriceListResult execute(UpdatePriceListCommand command) {
        PriceList priceList = priceListRepository.findById(command.priceListId())
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PriceListNotFound(command.priceListId().getValue()),
                        "Price list not found"
                ));
        priceList.update(
                command.title(),
                command.description(),
                PriceListStatus.valueOf(command.status().trim().toUpperCase()),
                PriceListType.valueOf(command.type().trim().toUpperCase()),
                command.startsAt(),
                command.endsAt(),
                Instant.now()
        );
        PriceList saved = priceListRepository.save(priceList);
        return PricingResultMapper.toPriceListResult(saved);
    }
}
