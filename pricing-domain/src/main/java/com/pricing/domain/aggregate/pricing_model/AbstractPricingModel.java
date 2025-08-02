package com.pricing.domain.aggregate.pricing_model;

import com.grab.framework.domain.Entity;
import com.pricing.domain.aggregate.PricingModel;

import java.io.Serializable;

public abstract class AbstractPricingModel<ID extends Serializable> extends Entity<ID> implements PricingModel {

    protected AbstractPricingModel(ID id) {
        super(id);
    }
}
