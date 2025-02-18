package com.pricing.infrastructure.mapper.price;

import org.joda.money.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MoneyMapper {
    public BigDecimal getMoney(Money money) {
        return money.getAmount();
    }
}
