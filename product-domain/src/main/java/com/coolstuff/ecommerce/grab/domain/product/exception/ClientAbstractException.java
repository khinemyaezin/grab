package com.coolstuff.ecommerce.grab.domain.exception;

import lombok.Getter;

@Getter
public abstract class ClientAbstractException extends RuntimeException{
    private final String prefix = "CLIENT_";
    private final String id;
    private final String shortMessage;
    public ClientAbstractException(Throwable e, String id, String shortMessage, String message) {
        super(message,e);
        this.id = this.prefix + id;
        this.shortMessage = shortMessage;
    }
    public ClientAbstractException(String id, String shortMessage, String message) {
        super(message);
        this.id = this.prefix + id;
        this.shortMessage = shortMessage;
    }
}
