package com.pricing.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.pricing.domain.aggregate.price_adjustment.AbstractPricingAdjustment;
import com.pricing.domain.aggregate.pricing_model.AbstractPricingModel;
import lombok.Getter;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class Pricing extends AggregateRoot<String> {
    private final String productId;
    private final List<AbstractPricingModel<String>> pricingModels;
    private final CurrencyUnit defaultCurrency;
    private final List<AbstractPricingAdjustment<String>> adjustments; // Taxes, Fees, Discounts
    private final PricingHistory pricingHistory;

    protected Pricing(Id id,String productId, CurrencyUnit defaultCurrency) {
        super(id);
        this.productId = productId;
        this.defaultCurrency = defaultCurrency;
        this.pricingModels = new ArrayList<>();
        this.adjustments = new ArrayList<>();
        this.pricingHistory = new PricingHistory();
    }

    public Money calculateFinalPrice(PricingContext context) {
        Money basePrice = calculateBasePrice(context);
        Money adjustedPrice = applyAdjustments(basePrice, context);
        pricingHistory.recordPriceCalculation(context, adjustedPrice);
        return adjustedPrice;
    }

    private Money calculateBasePrice(PricingContext context) {
        for (PricingModel model : pricingModels) {
            if (model.isApplicable(context)) {
                return model.calculatePrice(context);
            }
        }
        throw new IllegalStateException("No applicable pricing model found for context");
    }

    private Money applyAdjustments(Money basePrice, PricingContext context) {
        Money adjustedPrice = basePrice;
        for (PriceAdjustment adjustment : adjustments) {
            adjustedPrice = adjustment.apply(adjustedPrice, context);
        }
        return adjustedPrice;
    }

    public void addPricingModel(AbstractPricingModel<String> model) {
        pricingModels.add(model);
    }

    public void addAdjustment(AbstractPricingAdjustment<String> adjustment) {
        adjustments.add(adjustment);
    }

    public List<AbstractPricingModel<String>> getPricingModels(){
        return Collections.unmodifiableList(pricingModels);
    }

    public List<AbstractPricingAdjustment<String>> getPriceAdjustments(){
        return Collections.unmodifiableList(adjustments);
    }

}
