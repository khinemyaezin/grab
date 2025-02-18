package com.pricing.infrastructure.mapper.price;

import org.joda.money.CurrencyUnit;
import org.springframework.stereotype.Component;

@Component
public class CurrencyMapper {
    public String getCurrencyCode(CurrencyUnit currency) {
        return currency.getCode();
    }

    public CurrencyUnit getCurrencyUnit(String currencyCode) {
        return CurrencyUnit.of(currencyCode);
    }
}
