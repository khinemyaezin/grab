package com.pricing.adapter.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pricing.adapter.persistence.entity.PriceEntity;
import com.pricing.adapter.persistence.entity.PriceListEntity;
import com.pricing.adapter.persistence.entity.PriceListRuleEntity;
import com.pricing.adapter.persistence.entity.PricePreferenceEntity;
import com.pricing.adapter.persistence.entity.PriceRuleEntity;
import com.pricing.adapter.persistence.entity.PriceSetEntity;
import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.model.write.PricePreferenceResult;
import com.pricing.application.model.write.PriceSetResult;

import java.util.List;

public final class PricingEntityResultMapper {
    private PricingEntityResultMapper() {
    }

    public static PriceSetResult toPriceSetResult(PriceSetEntity entity) {
        return new PriceSetResult(
                entity.getUuid(),
                entity.getPrices().stream().map(PricingEntityResultMapper::toPriceSetPrice).toList()
        );
    }

    public static PriceListResult toPriceListResult(PriceListEntity entity, ObjectMapper objectMapper) {
        return new PriceListResult(
                entity.getUuid(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatus().name(),
                entity.getType().name(),
                entity.getStartsAt(),
                entity.getEndsAt(),
                entity.getRules().stream().map(rule -> toListRule(rule, objectMapper)).toList(),
                entity.getPrices().stream().map(PricingEntityResultMapper::toPriceListPrice).toList()
        );
    }

    public static PricePreferenceResult toPreferenceResult(PricePreferenceEntity entity) {
        return new PricePreferenceResult(
                entity.getUuid(),
                entity.getAttribute(),
                entity.getValue(),
                entity.isTaxInclusive()
        );
    }

    private static PriceSetResult.PriceResult toPriceSetPrice(PriceEntity entity) {
        return new PriceSetResult.PriceResult(
                entity.getUuid(),
                entity.getTitle(),
                entity.getCurrencyCode(),
                entity.getAmount(),
                entity.getMinQuantity(),
                entity.getMaxQuantity(),
                entity.getPriceSet().getUuid(),
                entity.getPriceList() == null ? null : entity.getPriceList().getUuid(),
                entity.getRules().stream().map(PricingEntityResultMapper::toRule).toList()
        );
    }

    private static PriceListResult.PriceResult toPriceListPrice(PriceEntity entity) {
        return new PriceListResult.PriceResult(
                entity.getUuid(),
                entity.getTitle(),
                entity.getCurrencyCode(),
                entity.getAmount(),
                entity.getMinQuantity(),
                entity.getMaxQuantity(),
                entity.getPriceSet().getUuid(),
                entity.getPriceList() == null ? null : entity.getPriceList().getUuid(),
                entity.getRules().stream().map(PricingEntityResultMapper::toRule).toList()
        );
    }

    private static PriceSetResult.PriceRuleResult toRule(PriceRuleEntity rule) {
        return new PriceSetResult.PriceRuleResult(
                rule.getUuid(),
                rule.getAttribute(),
                rule.getValue(),
                rule.getOperator().name(),
                rule.getPriority()
        );
    }

    private static PriceListResult.PriceListRuleResult toListRule(
            PriceListRuleEntity rule,
            ObjectMapper objectMapper
    ) {
        return new PriceListResult.PriceListRuleResult(
                rule.getUuid(),
                rule.getAttribute(),
                readValues(rule.getValuesJson(), objectMapper)
        );
    }

    private static List<String> readValues(String json, ObjectMapper objectMapper) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to deserialize price list rule values", exception);
        }
    }
}
