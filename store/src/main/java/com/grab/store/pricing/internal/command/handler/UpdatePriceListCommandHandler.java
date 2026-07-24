package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.pricing.internal.command.PriceListResult;
import com.grab.store.pricing.internal.command.UpdatePriceListCommand;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.grab.store.pricing.internal.exception.PricingServiceError;
import com.grab.store.pricing.internal.exception.PricingServiceException;
import com.grab.store.pricing.internal.util.PricingResultMapper;
import com.pricing.domain.aggregate.PriceList;
import com.pricing.domain.enums.PriceListStatus;
import com.pricing.domain.enums.PriceListType;
import com.pricing.domain.repository.PriceListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class UpdatePriceListCommandHandler
        implements CommandHandler<UpdatePriceListCommand, PriceListResult> {

    private final PriceListRepository priceListRepository;

    @Override
    @PricingTransactional
    public PriceListResult handle(UpdatePriceListCommand command) {
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

    @Override
    public Class<UpdatePriceListCommand> getCommandType() {
        return UpdatePriceListCommand.class;
    }
}
