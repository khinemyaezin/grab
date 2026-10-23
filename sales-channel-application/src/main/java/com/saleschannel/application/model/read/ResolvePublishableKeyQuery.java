package com.saleschannel.application.model.read;

import com.grab.framework.cqrs.query.Query;

import java.util.Optional;

public record ResolvePublishableKeyQuery(String rawToken) implements Query<Optional<ResolvedPublishableKeyView>> {
    @Override
    public String toString() {
        return "ResolvePublishableKeyQuery[rawToken=[REDACTED]]";
    }
}
