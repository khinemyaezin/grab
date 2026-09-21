package com.pricing.application.service;

import com.pricing.application.port.inbound.ReplacePriceListRulesUseCase;

import com.grab.framework.id.IdGenerator;
import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.model.write.ReplacePriceListRulesCommand;
import com.pricing.application.exception.PricingServiceError;
import com.pricing.application.exception.PricingServiceException;
import com.pricing.application.util.PricingResultMapper;
import com.pricing.domain.aggregate.PriceList;
import com.pricing.domain.entity.PriceListRule;
import com.pricing.domain.port.outbound.PriceListRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
public class ReplacePriceListRulesService implements ReplacePriceListRulesUseCase {

    private final PriceListRepository priceListRepository;
    private final IdGenerator idGenerator;
    public PriceListResult execute(ReplacePriceListRulesCommand command) {
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
}
