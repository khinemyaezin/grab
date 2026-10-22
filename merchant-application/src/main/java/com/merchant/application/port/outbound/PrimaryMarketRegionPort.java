package com.merchant.application.port.outbound;

public interface PrimaryMarketRegionPort {
    boolean hasActiveRegion(String countryCode);
}
