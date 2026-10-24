package com.grab.discovery.application.model.read;

public record ResolveShoppingContextResult(
        ShoppingContext context,
        boolean regionServed
) {
}
