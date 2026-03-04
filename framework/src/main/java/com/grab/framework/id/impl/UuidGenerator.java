package com.grab.framework.id.impl;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;

public class UuidGenerator implements IdGenerator {
    @Override
    public Id generateId() {
        return new CommonId();
    }

    @Override
    public Id generateId(String id) {
        return new CommonId(id);
    }
}