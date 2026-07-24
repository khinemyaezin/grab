package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.pricing.internal.command.AddPriceToPriceListCommand;
import com.grab.store.pricing.internal.command.PriceListResult;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.grab.store.pricing.internal.exception.PricingServiceError;
import com.grab.store.pricing.internal.exception.PricingServiceException;
import com.grab.store.pricing.internal.util.PricingResultMapper;
import com.pricing.domain.aggregate.PriceList;
import com.pricing.domain.entity.Price;
import com.pricing.domain.repository.PriceListRepository;
import com.pricing.domain.repository.PriceSetRepository;
import com.pricing.domain.valueobject.CurrencyCode;
import com.pricing.domain.valueobject.MoneyAmount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class AddPriceToPriceListCommandHandler
        implements CommandHandler<AddPriceToPriceListCommand, PriceListResult> {

    private final PriceListRepository priceListRepository;
    private final PriceSetRepository priceSetRepository;
    private final IdGenerator idGenerator;

    @Override
    @PricingTransactional
    public PriceListResult handle(AddPriceToPriceListCommand command) {
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

    @Override
    public Class<AddPriceToPriceListCommand> getCommandType() {
        return AddPriceToPriceListCommand.class;
    }
}
