package com.product.domain.event;

public record CategoryChangedEvent(
        String oldCategory,
        String newCategory,
        String productId
) implements Event {

}
