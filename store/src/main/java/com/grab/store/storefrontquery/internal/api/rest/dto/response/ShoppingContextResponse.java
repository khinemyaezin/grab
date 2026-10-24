package com.grab.store.storefrontquery.internal.api.rest.dto.response;

import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "shoppingContexts", itemRelation = "shoppingContext")
public record ShoppingContextResponse(
        String salesChannelId,
        String regionId,
        String countryCode,
        String currencyCode,
        String locale,
        boolean regionServed
) {
}
