package com.product.domain.service;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

public interface CurrencyExchange {
    Money convert(Money amount, CurrencyUnit targetCurrency);
}
