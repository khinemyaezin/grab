package com.product.domain.service;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class CurrencyConverter {
    private final Map<CurrencyUnit, BigDecimal> exchangeRates; // Currency code -> Exchange rate

    public CurrencyConverter(Map<CurrencyUnit, BigDecimal> exchangeRates) {
        this.exchangeRates = exchangeRates;
    }

    public Money convert(Money amount, CurrencyUnit targetCurrency) {
        BigDecimal rate = exchangeRates.get(targetCurrency);
        return amount.multipliedBy(rate, RoundingMode.HALF_UP);
    }
}
