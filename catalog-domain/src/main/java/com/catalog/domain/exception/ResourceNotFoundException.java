package com.catalog.domain.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends Exception {
    private final String id;
    private final String shortMessage;

    public ResourceNotFoundException(Throwable e, ErrorCode error, String... params) {
        super(String.format(error.getMessage(), (Object) params), e);
        this.id = error.getId();
        this.shortMessage = error.getShortMessage();
    }

    public ResourceNotFoundException(ErrorCode error, String... params) {
        super(String.format(error.getMessage(), (Object) params));
        this.id = error.getId();
        this.shortMessage = error.getShortMessage();
    }
}
