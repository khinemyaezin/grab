package com.grab.store.catalog.internal.util;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.catalog.internal.assembler.CommonId;

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