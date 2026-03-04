package com.grab.framework.mapper;

import com.grab.framework.id.Id;

public class IdMapper {
    public String map(Id id) {
        return id != null ? id.getValue() : null;
    }
}
