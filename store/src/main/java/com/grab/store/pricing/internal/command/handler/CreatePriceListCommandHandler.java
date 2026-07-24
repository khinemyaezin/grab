package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.pricing.internal.command.CreatePriceListCommand;
import com.grab.store.pricing.internal.command.PriceListResult;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.grab.store.pricing.internal.util.PricingResultMapper;
import com.pricing.domain.aggregate.PriceList;
import com.pricing.domain.enums.PriceListType;
import com.pricing.domain.repository.PriceListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class CreatePriceListCommandHandler
        implements CommandHandler<CreatePriceListCommand, PriceListResult> {

    private final PriceListRepository priceListRepository;
    private final IdGenerator idGenerator;

    @Override
    @PricingTransactional
    public PriceListResult handle(CreatePriceListCommand command) {
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

    @Override
    public Class<CreatePriceListCommand> getCommandType() {
        return CreatePriceListCommand.class;
    }
}
