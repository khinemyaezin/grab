package com.pricing.domain.aggregate;

import org.joda.money.Money;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PricingHistory {
    private final List<PriceCalculationRecord> records = new ArrayList<>();

    public void recordPriceCalculation(PricingContext context, Money finalPrice) {
        records.add(new PriceCalculationRecord(context, finalPrice, LocalDateTime.now()));
    }

    protected List<PriceCalculationRecord> getRecords() {
        return records;
    }
}
