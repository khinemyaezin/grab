package com.pricing.domain.valueobject;

import java.util.List;

public final class PricingAttributeKeys {

    public static final String REGION_ID = "region_id";
    public static final String CURRENCY_CODE = "currency_code";
    public static final String CUSTOMER_GROUP_ID = "customer_group_id";
    public static final String SALES_CHANNEL_ID = "sales_channel_id";

    private PricingAttributeKeys() {
    }

    public static List<String> wellKnown() {
        return List.of(REGION_ID, CURRENCY_CODE, CUSTOMER_GROUP_ID, SALES_CHANNEL_ID);
    }

    public static List<String> taxPreferenceLookupOrder() {
        return List.of(REGION_ID, CURRENCY_CODE);
    }
}
