package com.grab.framework.id;

public interface IdGenerator {
    Id generateId();
    Id convertIdFrom(String id);
}
