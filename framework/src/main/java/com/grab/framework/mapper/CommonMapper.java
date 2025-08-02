package com.grab.framework.mapper;

import com.grab.framework.id.Id;

public class CommonMapper {

    public String getUUID(Id id){
        return id.getValue();
    }
}
