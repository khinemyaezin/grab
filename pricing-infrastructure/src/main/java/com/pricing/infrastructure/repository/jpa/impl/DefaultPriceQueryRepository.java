package com.pricing.infrastructure.repository.jpa.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.grab.framework.support.PersistenceExecutor;
import com.pricing.domain.policy.PriceCandidate;
import com.pricing.domain.policy.PricePreferenceView;
import com.pricing.infrastructure.entity.PriceEntity;
import com.pricing.infrastructure.entity.PriceListEntity;
import com.pricing.infrastructure.entity.PriceListRuleEntity;
import com.pricing.infrastructure.entity.PricePreferenceEntity;
import com.pricing.infrastructure.repository.jpa.PriceJpaRepository;
import com.pricing.infrastructure.repository.jpa.PricePreferenceJpaRepository;
import com.pricing.infrastructure.repository.jpa.PriceQueryRepository;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class DefaultPriceQueryRepository implements PriceQueryRepository {
    private final PriceJpaRepository prices;
    private final PricePreferenceJpaRepository preferences;
    private final IdMapper ids;
    private final ObjectMapper objectMapper;
    private final PersistenceExecutor executor;

    @Override
    public List<PriceCandidate> findCandidates(Collection<Id> priceSetIds, String currencyCode) {
        return executor.query("PriceCandidate", () -> {
            List<String> uuids = priceSetIds.stream().map(Id::getValue).toList();
            return prices.findCandidates(uuids, currencyCode).stream()
                    .map(this::toCandidate)
                    .toList();
        });
    }

    @Override
    public List<PricePreferenceView> findPreferences() {
        return executor.query("PricePreference", () ->
                preferences.findAll().stream().map(this::toPreferenceView).toList());
    }

    private PriceCandidate toCandidate(PriceEntity entity) {
        PriceListEntity priceList = entity.getPriceList();
        List<PriceCandidate.RuleCondition> priceRules = entity.getRules().stream()
                .map(rule -> new PriceCandidate.RuleCondition(
                        rule.getAttribute(),
                        rule.getValue(),
                        rule.getOperator()
                ))
                .toList();
        List<PriceCandidate.ListRuleCondition> listRules = priceList == null
                ? List.of()
                : priceList.getRules().stream().map(this::toListRule).toList();
        return new PriceCandidate(
                ids.map(entity.getUuid()),
                ids.map(entity.getPriceSet().getUuid()),
                priceList == null ? null : ids.map(priceList.getUuid()),
                priceList == null ? null : priceList.getType(),
                priceList == null ? null : priceList.getStatus(),
                priceList == null ? null : priceList.getStartsAt(),
                priceList == null ? null : priceList.getEndsAt(),
                entity.getCurrencyCode(),
                entity.getAmount(),
                entity.getMinQuantity(),
                entity.getMaxQuantity(),
                entity.getRulesCount(),
                priceList == null ? 0 : priceList.getRulesCount(),
                priceRules,
                listRules
        );
    }

    private PriceCandidate.ListRuleCondition toListRule(PriceListRuleEntity rule) {
        return new PriceCandidate.ListRuleCondition(rule.getAttribute(), readValues(rule.getValuesJson()));
    }

    private PricePreferenceView toPreferenceView(PricePreferenceEntity entity) {
        return new PricePreferenceView(
                ids.map(entity.getUuid()),
                entity.getAttribute(),
                entity.getValue(),
                entity.isTaxInclusive()
        );
    }

    private List<String> readValues(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to deserialize price list rule values", exception);
        }
    }
}
