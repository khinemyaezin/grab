package com.grab.store.product.internal.assembler;

import com.grab.framework.id.Id;

import java.util.UUID;

public class CommonId implements Id {
    private final UUID id;

    public CommonId(){
        this.id = UUID.randomUUID();
    }
    public String getValue() {
        return id.toString();
    }
}
