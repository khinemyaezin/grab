package com.pricing.domain.policy;

import com.grab.framework.id.Id;

public record PricePreferenceView(
        Id id,
        String attribute,
        String value,
        boolean taxInclusive
) {
}
