package com.inventory.domain.exception;
import com.grab.framework.exception.DomainException;
import com.grab.framework.service.MessageSource;
import lombok.Getter;

import java.util.Map;

@Getter
public class InsufficientQuantityException extends DomainException {
    private final int available;
    private final int requested;

    public InsufficientQuantityException(int available, int requested) {
        super(new InsufficientQuantityError( available, requested) ,
                "Insufficient Quantity" + available + " for " + requested + ".");
        this.available = available;
        this.requested = requested;
    }

    record InsufficientQuantityError(int available, int requested) implements MessageSource {
        private static final String CODE = "exception.inventory.insufficient_quantity_error";
        @Override
        public String code() {
            return CODE;
        }

        @Override
        public Map<String, Object> args() {
            return Map.of( "available", available, "requested", requested);
        }
    }
}