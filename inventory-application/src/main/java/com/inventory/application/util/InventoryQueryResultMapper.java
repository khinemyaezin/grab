package com.inventory.application.util;

import com.grab.framework.id.IdGenerator;
import com.inventory.application.model.read.GetBinResult;
import com.inventory.application.model.read.GetInventoryResult;
import com.inventory.application.model.read.GetLocationResult;
import com.inventory.application.model.read.GetZoneResult;
import com.inventory.application.model.read.BinView;
import com.inventory.application.model.read.InventoryItemView;
import com.inventory.application.model.read.LocationView;
import com.inventory.application.model.read.ZoneView;

public final class InventoryQueryResultMapper {
    private InventoryQueryResultMapper() {
    }

    public static int availableQuantity(InventoryItemView view) {
        return view.onHand() - view.reserved() - view.damaged();
    }

    public static GetInventoryResult toInventoryResult(
            InventoryItemView view,
            String productName,
            IdGenerator idGenerator
    ) {
        return new GetInventoryResult(
                idGenerator.convertIdFrom(view.uuid()),
                view.sku(),
                productName,
                idGenerator.convertIdFrom(view.locationId()),
                view.locationCode(),
                view.locationName(),
                view.onHand(),
                view.reserved(),
                view.inTransit(),
                view.damaged(),
                availableQuantity(view),
                view.status() == null ? null : view.status().name(),
                view.safetyStock(),
                view.reorderPoint(),
                view.reorderQuantity(),
                view.maxStock()
        );
    }

    public static GetLocationResult toLocationResult(LocationView view, IdGenerator idGenerator) {
        return new GetLocationResult(
                idGenerator.convertIdFrom(view.uuid()),
                view.code(),
                view.name(),
                view.type() == null ? null : view.type().name(),
                view.active(),
                new GetLocationResult.Address(
                        view.street(),
                        view.street2(),
                        view.city(),
                        view.state(),
                        view.postalCode(),
                        view.country()
                )
        );
    }

    public static GetBinResult toBinResult(BinView view, IdGenerator idGenerator) {
        return new GetBinResult(
                idGenerator.convertIdFrom(view.uuid()),
                idGenerator.convertIdFrom(view.zoneId()),
                view.code(),
                view.name(),
                view.maxCapacity(),
                view.active()
        );
    }

    public static GetZoneResult toZoneResult(ZoneView view, IdGenerator idGenerator) {
        return new GetZoneResult(
                idGenerator.convertIdFrom(view.uuid()),
                idGenerator.convertIdFrom(view.locationId()),
                view.code(),
                view.name(),
                view.type() == null ? null : view.type().name(),
                view.active()
        );
    }
}
