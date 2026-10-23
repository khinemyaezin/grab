package com.grab.discovery.application.model.read;

import com.grab.framework.cqrs.query.Query;

public record ResolveShoppingContextQuery(
        String publishableKey,
        String countryCode,
        String locale
) implements Query<ResolveShoppingContextResult> {
    @Override
    public String toString() {
        return "ResolveShoppingContextQuery[publishableKey=[REDACTED], countryCode="
                + countryCode + ", locale=" + locale + "]";
    }
}
