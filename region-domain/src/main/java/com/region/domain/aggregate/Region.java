package com.region.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.region.domain.enums.RegionStatus;
import lombok.Getter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
public class Region extends AggregateRoot<Id> {
    private final String name;
    private final String currencyCode;
    private final RegionStatus status;
    private final Set<String> countryCodes;

    public Region(
            Id id,
            String name,
            String currencyCode,
            RegionStatus status,
            Set<String> countryCodes
    ) {
        super(id);
        this.name = Objects.requireNonNull(name, "name is required");
        this.currencyCode = Objects.requireNonNull(currencyCode, "currencyCode is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.countryCodes = countryCodes == null ? Set.of() : Set.copyOf(countryCodes);
    }

    public boolean isActive() {
        return status == RegionStatus.ACTIVE;
    }

    public boolean allowsCountry(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return false;
        }
        return countryCodes.contains(countryCode.trim().toUpperCase());
    }
}
