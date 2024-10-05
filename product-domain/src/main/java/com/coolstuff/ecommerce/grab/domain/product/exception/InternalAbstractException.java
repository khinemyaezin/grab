package com.coolstuff.ecommerce.grab.domain.exception;

import lombok.Getter;

@Getter
public abstract class InternalAbstractException extends RuntimeException{
    private final String prefix = "SERVER_";
    private final String id;
    private final String shortMessage;
    public InternalAbstractException(Throwable e, String id, String shortMessage, String message) {
        super(message,e);
        this.id = this.prefix + id;
        this.shortMessage = shortMessage;
    }
    public InternalAbstractException(String id, String shortMessage, String message) {
        super(message);
        this.id = this.prefix + id;
        this.shortMessage = shortMessage;
    }
}
