package com.grab.framework.cqrs;

public record Page<T>(
        T record,
        PageInfo pageInfo
){

}
