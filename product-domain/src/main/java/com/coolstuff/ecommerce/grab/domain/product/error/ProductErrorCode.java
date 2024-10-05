package com.coolstuff.ecommerce.grab.domain.product.error;

import com.coolstuff.ecommerce.grab.domain.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProductErrorCode implements ErrorCode {
    P000001("100001", "Not found", "Product is not found"),
    P000002("100002", "Internal server error", "Internal server error.");
    private final String id;
    private final String shortMessage;
    private final String message;
}
