package com.grab.store.catalog.internal.exception;

import com.catalog.domain.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProductError implements ErrorCode {
    notFound("","Not Found","Request resource is not found");
    private final String id;
    private final String shortMessage;
    private final String message;
}
