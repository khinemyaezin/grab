package com.pricing.application.service;

import com.pricing.application.port.inbound.AddPriceToPriceListUseCase;

import com.grab.framework.id.IdGenerator;
import com.pricing.application.model.write.AddPriceToPriceListCommand;
import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.exception.PricingServiceError;
import com.pricing.application.exception.PricingServiceException;
import com.pricing.application.util.PricingResultMapper;
import com.pricing.domain.aggregate.PriceList;
import com.pricing.domain.entity.Price;
import com.pricing.domain.port.outbound.PriceListRepository;
import com.pricing.domain.port.outbound.PriceSetRepository;
import com.pricing.domain.valueobject.CurrencyCode;
import com.pricing.domain.valueobject.MoneyAmount;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class AddPriceToPriceListService implements AddPriceToPriceListUseCase {

    private final PriceListRepository priceListRepository;
    private final PriceSetRepository priceSetRepository;
    private final IdGenerator idGenerator;
    public PriceListResult execute(AddPriceToPriceListCommand command) {
        PriceList priceList = priceListRepository.findById(command.priceListId())
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PriceListNotFound(command.priceListId().getValue()),
                        "Price list not found"
                ));
        priceSetRepository.findById(command.priceSetId())
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PriceSetNotFound(command.priceSetId().getValue()),
                        "Price set not found"
                ));
        Price price = Price.createCampaign(
                idGenerator.generateId(),
                command.priceSetId(),
                priceList.getId(),
                command.title(),
                CurrencyCode.of(command.currencyCode()),
                MoneyAmount.of(command.amount()),
                command.minQuantity(),
                command.maxQuantity(),
                PricingResultMapper.toRules(idGenerator, command.rules())
        );
        priceList.addPrice(price, Instant.now());
        PriceList saved = priceListRepository.save(priceList);
        return PricingResultMapper.toPriceListResult(saved);
    }
}
