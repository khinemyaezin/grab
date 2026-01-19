package com.grab.framework.utility;

public record Error(
        String errorCode,
        Object... args
){
    static Error of(String errorCode, Object... args){
        return new Error(errorCode, args);
    }
}