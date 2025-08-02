package com.pricing.infrastructure.entity;

import jakarta.persistence.Embeddable;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.BigDecimal;

@Embeddable
public class MoneyEmbeddable {

    private String currency;

    private BigDecimal amount;

    public Money toMoney() {
        return Money.of(CurrencyUnit.of(currency), amount);
    }

    public static MoneyEmbeddable fromMoney(Money money) {
        MoneyEmbeddable embeddable = new MoneyEmbeddable();
        embeddable.currency = money.getCurrencyUnit().getCode();
        embeddable.amount = money.getAmount();
        return embeddable;
    }
}
