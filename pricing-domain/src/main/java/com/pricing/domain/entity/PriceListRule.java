package com.pricing.domain.entity;

import com.grab.framework.domain.Entity;
import com.grab.framework.id.Id;
import com.pricing.domain.exception.PricingDomainError;
import com.pricing.domain.exception.PricingDomainException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
public class PriceListRule extends Entity<Id> {
    private final String attribute;
    private final List<String> values;

    public PriceListRule(Id id, String attribute, List<String> values) {
        super(id);
        if (attribute == null || attribute.isBlank()) {
            throw new PricingDomainException(
                    new PricingDomainError.InvalidField("attribute"),
                    "price list rule attribute is required"
            );
        }
        if (values == null || values.isEmpty()) {
            throw new PricingDomainException(
                    new PricingDomainError.InvalidField("values"),
                    "price list rule values are required"
            );
        }
        this.attribute = attribute.trim();
        this.values = List.copyOf(values);
    }

    public boolean matches(Map<String, String> attributes) {
        String contextValue = attributes.get(attribute);
        if (contextValue == null) {
            return false;
        }
        return values.contains(contextValue);
    }

    public List<String> getValues() {
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
