package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.pricing.internal.command.PriceListResult;
import com.grab.store.pricing.internal.command.ReplacePriceListRulesCommand;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.grab.store.pricing.internal.exception.PricingServiceError;
import com.grab.store.pricing.internal.exception.PricingServiceException;
import com.grab.store.pricing.internal.util.PricingResultMapper;
import com.pricing.domain.aggregate.PriceList;
import com.pricing.domain.entity.PriceListRule;
import com.pricing.domain.repository.PriceListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class ReplacePriceListRulesCommandHandler
        implements CommandHandler<ReplacePriceListRulesCommand, PriceListResult> {

    private final PriceListRepository priceListRepository;
    private final IdGenerator idGenerator;

    @Override
    @PricingTransactional
    public PriceListResult handle(ReplacePriceListRulesCommand command) {
        PriceList priceList = priceListRepository.findById(command.priceListId())
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PriceListNotFound(command.priceListId().getValue()),
                        "Price list not found"
                ));
        List<PriceListRule> rules = command.rules().stream()
                .map(rule -> new PriceListRule(idGenerator.generateId(), rule.attribute(), rule.values()))
                .toList();
        priceList.replaceRules(rules, Instant.now());
        PriceList saved = priceListRepository.save(priceList);
        return PricingResultMapper.toPriceListResult(saved);
    }

    @Override
    public Class<ReplacePriceListRulesCommand> getCommandType() {
        return ReplacePriceListRulesCommand.class;
    }
}
