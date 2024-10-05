package com.coolstuff.ecommerce.grab.domain.exception;

public interface ErrorCode {
    String getId();
    String getShortMessage();
    String getMessage();
}
