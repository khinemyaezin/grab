package com.pricing.infrastructure.mapper.jpa.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.pricing.domain.aggregate.PriceList;
import com.pricing.domain.aggregate.PricePreference;
import com.pricing.domain.aggregate.PriceSet;
import com.pricing.domain.entity.Price;
import com.pricing.domain.entity.PriceListRule;
import com.pricing.domain.entity.PriceRule;
import com.pricing.domain.valueobject.CurrencyCode;
import com.pricing.domain.valueobject.MoneyAmount;
import com.pricing.infrastructure.entity.PriceEntity;
import com.pricing.infrastructure.entity.PriceListEntity;
import com.pricing.infrastructure.entity.PriceListRuleEntity;
import com.pricing.infrastructure.entity.PricePreferenceEntity;
import com.pricing.infrastructure.entity.PriceRuleEntity;
import com.pricing.infrastructure.entity.PriceSetEntity;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PricingJpaAssembler {
    private final IdMapper ids;
    private final ObjectMapper objectMapper;

    public PriceSet toDomain(PriceSetEntity source) {
        List<Price> prices = source.getPrices().stream()
                .map(price -> toDomainPrice(price, source.getUuid(), null))
                .toList();
        return new PriceSet(
                ids.map(source.getUuid()),
                prices,
                source.getCreatedAt(),
                source.getUpdatedAt(),
                source.getVersion()
        );
    }

    public PriceSetEntity toEntity(PriceSet source, PriceSetEntity destination) {
        PriceSetEntity entity = destination == null ? new PriceSetEntity() : destination;
        if (entity.getUuid() == null) {
            entity.setUuid(source.getId().getValue());
            entity.setCreatedAt(source.getCreatedAt());
        }
        entity.setUpdatedAt(source.getUpdatedAt());

        Map<String, PriceEntity> existingByUuid = entity.getPrices().stream()
                .collect(Collectors.toMap(PriceEntity::getUuid, Function.identity()));
        List<PriceEntity> nextPrices = new ArrayList<>();
        Instant now = source.getUpdatedAt();
        for (Price price : source.getPrices()) {
            PriceEntity priceEntity = existingByUuid.getOrDefault(price.getId().getValue(), new PriceEntity());
            applyPrice(price, priceEntity, entity, null, now);
            nextPrices.add(priceEntity);
        }
        entity.getPrices().clear();
        entity.getPrices().addAll(nextPrices);
        return entity;
    }

    public PriceList toDomain(PriceListEntity source) {
        List<PriceListRule> rules = source.getRules().stream()
                .map(this::toDomainListRule)
                .toList();
        List<Price> prices = source.getPrices().stream()
                .map(price -> toDomainPrice(price, price.getPriceSet().getUuid(), source.getUuid()))
                .toList();
        return new PriceList(
                ids.map(source.getUuid()),
                source.getTitle(),
                source.getDescription(),
                source.getStatus(),
                source.getType(),
                source.getStartsAt(),
                source.getEndsAt(),
                rules,
                prices,
                source.getCreatedAt(),
                source.getUpdatedAt(),
                source.getVersion()
        );
    }

    public PriceListEntity toEntity(
            PriceList source,
            PriceListEntity destination,
            Function<String, PriceSetEntity> priceSetLookup
    ) {
        PriceListEntity entity = destination == null ? new PriceListEntity() : destination;
        if (entity.getUuid() == null) {
            entity.setUuid(source.getId().getValue());
            entity.setCreatedAt(source.getCreatedAt());
        }
        entity.setTitle(source.getTitle());
        entity.setDescription(source.getDescription());
        entity.setStatus(source.getStatus());
        entity.setType(source.getType());
        entity.setStartsAt(source.getStartsAt());
        entity.setEndsAt(source.getEndsAt());
        entity.setRulesCount(source.rulesCount());
        entity.setUpdatedAt(source.getUpdatedAt());

        Map<String, PriceListRuleEntity> existingRules = entity.getRules().stream()
                .collect(Collectors.toMap(PriceListRuleEntity::getUuid, Function.identity()));
        List<PriceListRuleEntity> nextRules = new ArrayList<>();
        for (PriceListRule rule : source.getRules()) {
            PriceListRuleEntity ruleEntity = existingRules.getOrDefault(rule.getId().getValue(), new PriceListRuleEntity());
            if (ruleEntity.getUuid() == null) {
                ruleEntity.setUuid(rule.getId().getValue());
            }
            ruleEntity.setPriceList(entity);
            ruleEntity.setAttribute(rule.getAttribute());
            ruleEntity.setValuesJson(writeValues(rule.getValues()));
            nextRules.add(ruleEntity);
        }
        entity.getRules().clear();
        entity.getRules().addAll(nextRules);

        Map<String, PriceEntity> existingPrices = entity.getPrices().stream()
                .collect(Collectors.toMap(PriceEntity::getUuid, Function.identity()));
        List<PriceEntity> nextPrices = new ArrayList<>();
        Instant now = source.getUpdatedAt();
        for (Price price : source.getPrices()) {
            PriceEntity priceEntity = existingPrices.getOrDefault(price.getId().getValue(), new PriceEntity());
            PriceSetEntity priceSet = priceSetLookup.apply(price.getPriceSetId().getValue());
            applyPrice(price, priceEntity, priceSet, entity, now);
            nextPrices.add(priceEntity);
        }
        entity.getPrices().clear();
        entity.getPrices().addAll(nextPrices);
        return entity;
    }

    public PricePreference toDomain(PricePreferenceEntity source) {
        return new PricePreference(
                ids.map(source.getUuid()),
                source.getAttribute(),
                source.getValue(),
                source.isTaxInclusive(),
                source.getCreatedAt(),
                source.getUpdatedAt(),
                source.getVersion()
        );
    }

    public PricePreferenceEntity toEntity(PricePreference source, PricePreferenceEntity destination) {
        PricePreferenceEntity entity = destination == null ? new PricePreferenceEntity() : destination;
        if (entity.getUuid() == null) {
            entity.setUuid(source.getId().getValue());
            entity.setCreatedAt(source.getCreatedAt());
        }
        entity.setAttribute(source.getAttribute());
        entity.setValue(source.getValue());
        entity.setTaxInclusive(source.isTaxInclusive());
        entity.setUpdatedAt(source.getUpdatedAt());
        return entity;
    }

    private Price toDomainPrice(PriceEntity source, String priceSetUuid, String priceListUuid) {
        List<PriceRule> rules = source.getRules().stream()
                .map(this::toDomainRule)
                .toList();
        Id priceListId = priceListUuid == null ? null : ids.map(priceListUuid);
        return new Price(
                ids.map(source.getUuid()),
                source.getTitle(),
                CurrencyCode.of(source.getCurrencyCode()),
                MoneyAmount.of(source.getAmount()),
                source.getMinQuantity(),
                source.getMaxQuantity(),
                ids.map(priceSetUuid),
                priceListId,
                rules
        );
    }

    private PriceRule toDomainRule(PriceRuleEntity source) {
        return new PriceRule(
                ids.map(source.getUuid()),
                source.getAttribute(),
                source.getValue(),
                source.getOperator(),
                source.getPriority()
        );
    }

    private PriceListRule toDomainListRule(PriceListRuleEntity source) {
        return new PriceListRule(
                ids.map(source.getUuid()),
                source.getAttribute(),
                readValues(source.getValuesJson())
        );
    }

    private void applyPrice(
            Price price,
            PriceEntity entity,
            PriceSetEntity priceSet,
            PriceListEntity priceList,
            Instant now
    ) {
        if (entity.getUuid() == null) {
            entity.setUuid(price.getId().getValue());
            entity.setCreatedAt(now);
        }
        entity.setTitle(price.getTitle());
        entity.setCurrencyCode(price.getCurrencyCode().value());
        entity.setAmount(price.getAmount().value());
        entity.setMinQuantity(price.getMinQuantity());
        entity.setMaxQuantity(price.getMaxQuantity());
        entity.setRulesCount(price.rulesCount());
        entity.setPriceSet(priceSet);
        entity.setPriceList(priceList);
        entity.setUpdatedAt(now);

        Map<String, PriceRuleEntity> existingRules = entity.getRules().stream()
                .collect(Collectors.toMap(PriceRuleEntity::getUuid, Function.identity(), (left, right) -> left, HashMap::new));
        List<PriceRuleEntity> nextRules = new ArrayList<>();
        for (PriceRule rule : price.getRules()) {
            PriceRuleEntity ruleEntity = existingRules.getOrDefault(rule.getId().getValue(), new PriceRuleEntity());
            if (ruleEntity.getUuid() == null) {
                ruleEntity.setUuid(rule.getId().getValue());
            }
            ruleEntity.setPrice(entity);
            ruleEntity.setAttribute(rule.getAttribute());
            ruleEntity.setValue(rule.getValue());
            ruleEntity.setOperator(rule.getOperator());
            ruleEntity.setPriority(rule.getPriority());
            nextRules.add(ruleEntity);
        }
        entity.getRules().clear();
        entity.getRules().addAll(nextRules);
    }

    private String writeValues(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize price list rule values", exception);
        }
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
