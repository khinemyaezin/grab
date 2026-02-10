package com.catalog.domain.exception;

import lombok.Getter;

@Getter
public class DomainException extends Exception {
    private final String id;
    private final String shortMessage;

    public DomainException(Throwable e, ErrorCode error, String... params) {
        super(String.format(error.getMessage(), (Object) params), e);
        this.id = error.getId();
        this.shortMessage = error.getShortMessage();
    }

    public DomainException(ErrorCode error, String... params) {
        super(String.format(error.getMessage(), (Object) params));
        this.id = error.getId();
        this.shortMessage = error.getShortMessage();
    }
}
