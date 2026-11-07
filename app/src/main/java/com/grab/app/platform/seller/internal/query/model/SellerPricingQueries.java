package com.grab.app.platform.seller.internal.query.model;

import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.CalculatedPriceOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PriceListOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PricePreferenceOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PriceSetOutboundResult;
import com.grab.framework.cqrs.query.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SellerPricingQueries {

    private SellerPricingQueries() {}

    public record GetSellerPriceListQuery(
            String priceListId
    ) implements Query<Optional<PriceListOutboundResult>> {}

    public record ListSellerPriceListsQuery() implements Query<List<PriceListOutboundResult>> {}

    public record GetSellerPriceSetQuery(
            String priceSetId
    ) implements Query<Optional<PriceSetOutboundResult>> {}

    public record CalculateSellerPricesQuery(
            List<String> priceSetIds,
            String currencyCode,
            Integer quantity,
            Map<String, String> attributes
    ) implements Query<List<CalculatedPriceOutboundResult>> {}

    public record GetSellerPricePreferenceQuery(
            String preferenceId
    ) implements Query<Optional<PricePreferenceOutboundResult>> {}
}

