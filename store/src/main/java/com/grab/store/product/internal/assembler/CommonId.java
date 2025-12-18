package com.grab.store.product.internal.assembler;

import com.grab.framework.id.Id;

import java.util.UUID;

public class CommonId implements Id {
    private final String id;

    public CommonId() {
        this.id = UUID.randomUUID().toString();
    }

    public CommonId(String id) {
        this.id = id;
    }

    public String getValue() {
        return id;
    }
}