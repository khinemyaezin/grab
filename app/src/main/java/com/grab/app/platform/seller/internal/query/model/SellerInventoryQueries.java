package com.grab.app.platform.seller.internal.query.model;

import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityBinOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityLocationOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityZoneOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.InventoryItemOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.ReorderSuggestionOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.StockMovementOutboundResult;
import com.grab.framework.cqrs.query.Query;

import java.util.List;
import java.util.Optional;

public final class SellerInventoryQueries {

    private SellerInventoryQueries() {}

    public record GetSellerInventoryItemQuery(
            String itemId
    ) implements Query<Optional<InventoryItemOutboundResult>> {}

    public record GetSellerStockMovementQuery(
            String itemId
    ) implements Query<Optional<StockMovementOutboundResult>> {}

    public record GetSellerLocationQuery(
            String locationId
    ) implements Query<Optional<FacilityLocationOutboundResult>> {}

    public record GetSellerZoneQuery(
            String zoneId
    ) implements Query<Optional<FacilityZoneOutboundResult>> {}

    public record GetSellerBinQuery(
            String binId
    ) implements Query<Optional<FacilityBinOutboundResult>> {}

    public record ListSellerReorderSuggestionsQuery(
            String merchantId,
            String locationId,
            String sku
    ) implements Query<List<ReorderSuggestionOutboundResult>> {}
}

