package com.customer.domain.entity;

import com.grab.framework.id.Id;
import lombok.Getter;

import java.util.Objects;

@Getter
public class CustomerAddress {
    private final Id id;
    private final String line1;
    private final String city;
    private final String country;
    private final String phone;
    private final boolean defaultAddress;

    public CustomerAddress(Id id, String line1, String city, String country, String phone, boolean defaultAddress) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.line1 = Objects.requireNonNull(line1, "line1 is required");
        this.city = Objects.requireNonNull(city, "city is required");
        this.country = Objects.requireNonNull(country, "country is required");
        this.phone = Objects.requireNonNull(phone, "phone is required");
        this.defaultAddress = defaultAddress;
    }
}
