package com.grab.framework.mapper;

import com.grab.framework.id.Id;

import java.util.Optional;

public class CommonMapper {

    public String getUUID(Optional<Id> id){
        return id.get().getValue();
    }
}
