package com.grab.discovery.adapter.persistence.entity;

import java.util.List;

public record DiscoveryProductOptionDocument(String name, List<String> values) {
    public DiscoveryProductOptionDocument {
        values = values == null ? List.of() : List.copyOf(values);
    }
}
