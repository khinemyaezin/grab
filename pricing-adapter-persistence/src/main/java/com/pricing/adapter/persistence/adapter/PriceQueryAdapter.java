package com.pricing.adapter.persistence.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.grab.framework.support.PersistenceExecutor;
import com.pricing.adapter.persistence.entity.PriceEntity;
import com.pricing.adapter.persistence.entity.PriceListEntity;
import com.pricing.adapter.persistence.entity.PriceListRuleEntity;
import com.pricing.adapter.persistence.entity.PricePreferenceEntity;
import com.pricing.adapter.persistence.mapper.PricingEntityResultMapper;
import com.pricing.adapter.persistence.repository.jpa.PriceJpaRepository;
import com.pricing.adapter.persistence.repository.jpa.PriceListJpaRepository;
import com.pricing.adapter.persistence.repository.jpa.PricePreferenceJpaRepository;
import com.pricing.adapter.persistence.repository.jpa.PriceSetJpaRepository;
import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.model.write.PricePreferenceResult;
import com.pricing.application.model.write.PriceSetResult;
import com.pricing.application.port.outbound.PriceQueryPort;
import com.pricing.domain.policy.PriceCandidate;
import com.pricing.domain.policy.PricePreferenceView;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class PriceQueryAdapter implements PriceQueryPort {
    private final PriceJpaRepository prices;
    private final PriceSetJpaRepository priceSets;
    private final PriceListJpaRepository priceLists;
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

    @Override
    public Optional<PriceSetResult> findPriceSetById(String priceSetId) {
        return executor.query("PriceSet", () ->
                priceSets.findByUuid(priceSetId).map(PricingEntityResultMapper::toPriceSetResult));
    }

    @Override
    public Optional<PriceListResult> findPriceListById(String priceListId) {
        return executor.query("PriceList", () ->
                priceLists.findByUuid(priceListId)
                        .map(entity -> PricingEntityResultMapper.toPriceListResult(entity, objectMapper)));
    }

    @Override
    public Optional<PricePreferenceResult> findPricePreferenceById(String pricePreferenceId) {
        return executor.query("PricePreference", () ->
                preferences.findByUuid(pricePreferenceId).map(PricingEntityResultMapper::toPreferenceResult));
    }

    @Override
    public List<PriceListResult> findAllPriceLists() {
        return executor.query("PriceList", () ->
                priceLists.findAllByOrderByCreatedAtAsc().stream()
                        .map(entity -> PricingEntityResultMapper.toPriceListResult(entity, objectMapper))
                        .toList());
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
