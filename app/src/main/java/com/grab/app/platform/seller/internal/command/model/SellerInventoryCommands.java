package com.grab.app.platform.seller.internal.command.model;

import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.AllocationOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityBinOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityLocationOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityZoneOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.InventoryItemOutboundResult;
import com.grab.framework.cqrs.command.Command;

public final class SellerInventoryCommands {

    private SellerInventoryCommands() {}

    public record CreateSellerInventoryItemCommand(
            String merchantId,
            String actorId,
            String sku,
            String locationId,
            int initialQuantity,
            Integer safetyStock,
            Integer reorderPoint,
            Integer reorderQuantity,
            Integer maxStock
    ) implements Command<InventoryItemOutboundResult> {}

    public record AllocateSellerStockCommand(
            String actorId,
            String sku,
            String locationId,
            int quantity,
            String orderId
    ) implements Command<AllocationOutboundResult> {}

    public record DeallocateSellerStockCommand(
            String actorId,
            String sku,
            int quantity,
            String orderId
    ) implements Command<Void> {}

    public record CreateSellerLocationCommand(
            String merchantId,
            String actorId,
            String code,
            String name,
            String type,
            String line1,
            String line2,
            String city,
            String state,
            String postalCode,
            String country
    ) implements Command<FacilityLocationOutboundResult> {}

    public record ActivateSellerLocationCommand(String locationId) implements Command<Void> {}

    public record DeactivateSellerLocationCommand(String locationId) implements Command<Void> {}

    public record DeleteSellerLocationCommand(String locationId) implements Command<Void> {}

    public record CreateSellerZoneCommand(
            String locationId,
            String actorId,
            String code,
            String name,
            String type
    ) implements Command<FacilityZoneOutboundResult> {}

    public record ActivateSellerZoneCommand(String zoneId) implements Command<Void> {}

    public record DeactivateSellerZoneCommand(String zoneId) implements Command<Void> {}

    public record DeleteSellerZoneCommand(String zoneId) implements Command<Void> {}

    public record CreateSellerBinCommand(
            String zoneId,
            String actorId,
            String code,
            String name,
            Integer maxCapacity
    ) implements Command<FacilityBinOutboundResult> {}

    public record ActivateSellerBinCommand(String binId) implements Command<Void> {}

    public record DeactivateSellerBinCommand(String binId) implements Command<Void> {}

    public record DeleteSellerBinCommand(String binId) implements Command<Void> {}
}
