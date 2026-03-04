package com.grab.store.inventory.internal.api.rest.controller;

import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.entity.InventoryReservation;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.enums.AdjustmentReason;
import com.inventory.domain.enums.StockMovementType;
import com.grab.store.inventory.internal.service.InventoryApplicationService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryApplicationService inventoryApplicationService;

    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(
            @RequestBody CreateInventoryRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        InventoryItem item = inventoryApplicationService.createInventory(
                request.sku(),
                request.productVariantId(),
                request.locationId(),
                request.initialQuantity(),
                request.safetyStock(),
                request.reorderPoint(),
                request.reorderQuantity(),
                request.maxStock(),
                actorId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(InventoryResponse.from(item));
    }

    @GetMapping("/{inventoryItemId}")
    public ResponseEntity<InventoryResponse> getInventory(@PathVariable String inventoryItemId) {
        return ResponseEntity.ok(InventoryResponse.from(inventoryApplicationService.getInventory(inventoryItemId)));
    }

    @PostMapping("/{inventoryItemId}/receive")
    public ResponseEntity<InventoryResponse> receiveStock(
            @PathVariable String inventoryItemId,
            @RequestBody ReceiveStockRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        InventoryItem item = inventoryApplicationService.receiveStock(
                inventoryItemId,
                request.quantity(),
                request.type(),
                request.referenceId(),
                actorId
        );
        return ResponseEntity.ok(InventoryResponse.from(item));
    }

    @PostMapping("/{inventoryItemId}/reserve")
    public ResponseEntity<InventoryReservationResponse> reserveStock(
            @PathVariable String inventoryItemId,
            @RequestBody ReserveStockRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        InventoryReservation reservation = inventoryApplicationService.reserveStock(
                inventoryItemId,
                request.quantity(),
                request.orderId(),
                request.orderLineId(),
                request.expiresAt(),
                idempotencyKey,
                actorId
        );
        return ResponseEntity.ok(InventoryReservationResponse.from(reservation));
    }

    @PostMapping("/{inventoryItemId}/reservations/{reservationId}/release")
    public ResponseEntity<InventoryReservationResponse> releaseReservation(
            @PathVariable String inventoryItemId,
            @PathVariable String reservationId,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        return ResponseEntity.ok(InventoryReservationResponse.from(
                inventoryApplicationService.releaseReservation(inventoryItemId, reservationId, actorId)
        ));
    }

    @PostMapping("/{inventoryItemId}/reservations/{reservationId}/ship")
    public ResponseEntity<InventoryReservationResponse> shipReservation(
            @PathVariable String inventoryItemId,
            @PathVariable String reservationId,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        return ResponseEntity.ok(InventoryReservationResponse.from(
                inventoryApplicationService.shipReservation(inventoryItemId, reservationId, actorId)
        ));
    }

    @PostMapping("/{inventoryItemId}/adjust")
    public ResponseEntity<InventoryResponse> adjustStock(
            @PathVariable String inventoryItemId,
            @RequestBody AdjustStockRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        return ResponseEntity.ok(InventoryResponse.from(
                inventoryApplicationService.adjustStock(inventoryItemId, request.newOnHandQuantity(), request.reason(), actorId)
        ));
    }

    @GetMapping("/{inventoryItemId}/movements")
    public ResponseEntity<List<StockMovementResponse>> getMovements(@PathVariable String inventoryItemId) {
        List<StockMovement> movements = inventoryApplicationService.getMovements(inventoryItemId);
        return ResponseEntity.ok(movements.stream().map(StockMovementResponse::from).toList());
    }

    @GetMapping("/{inventoryItemId}/reservations")
    public ResponseEntity<List<InventoryReservationResponse>> getReservations(@PathVariable String inventoryItemId) {
        List<InventoryReservation> reservations = inventoryApplicationService.getReservations(inventoryItemId);
        return ResponseEntity.ok(reservations.stream().map(InventoryReservationResponse::from).toList());
    }

    public record CreateInventoryRequest(
            @NotBlank String sku,
            String productVariantId,
            @NotBlank String locationId,
            @Min(0) int initialQuantity,
            Integer safetyStock,
            Integer reorderPoint,
            Integer reorderQuantity,
            Integer maxStock
    ) {}

    public record ReceiveStockRequest(
            @Min(1) int quantity,
            @NotNull StockMovementType type,
            String referenceId
    ) {}

    public record ReserveStockRequest(
            @Min(1) int quantity,
            @NotBlank String orderId,
            @NotBlank String orderLineId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expiresAt
    ) {}

    public record AdjustStockRequest(
            @Min(0) int newOnHandQuantity,
            @NotNull AdjustmentReason reason
    ) {}

    public record InventoryResponse(
            String id,
            String sku,
            String productVariantId,
            String locationId,
            int onHand,
            int reserved,
            int damaged,
            int available,
            String status,
            int safetyStock,
            int reorderPoint,
            int reorderQuantity,
            Integer maxStock
    ) {
        static InventoryResponse from(InventoryItem item) {
            return new InventoryResponse(
                    item.getId().getValue(),
                    item.getSku(),
                    item.getProductVariantId() == null ? null : item.getProductVariantId().getValue(),
                    item.getLocationId().getValue(),
                    item.getQuantity().onHand(),
                    item.getQuantity().reserved(),
                    item.getQuantity().damaged(),
                    item.getAvailableQuantity(),
                    item.getStatus().name(),
                    item.getReorderConfig().safetyStock(),
                    item.getReorderConfig().reorderPoint(),
                    item.getReorderConfig().reorderQuantity(),
                    item.getReorderConfig().maxStock()
            );
        }
    }

    public record InventoryReservationResponse(
            String id,
            String inventoryItemId,
            String orderId,
            String orderLineId,
            int quantity,
            String status,
            LocalDateTime expiresAt,
            String idempotencyKey
    ) {
        static InventoryReservationResponse from(InventoryReservation reservation) {
            return new InventoryReservationResponse(
                    reservation.getId().getValue(),
                    reservation.getInventoryItemId().getValue(),
                    reservation.getOrderId(),
                    reservation.getOrderLineId(),
                    reservation.getQuantity(),
                    reservation.getStatus().name(),
                    reservation.getExpiresAt(),
                    reservation.getIdempotencyKey()
            );
        }
    }

    public record StockMovementResponse(
            String id,
            String inventoryItemId,
            String type,
            int quantity,
            int quantityBefore,
            int quantityAfter,
            int onHandBefore,
            int onHandAfter,
            int reservedBefore,
            int reservedAfter,
            String referenceId,
            LocalDateTime createdAt
    ) {
        static StockMovementResponse from(StockMovement movement) {
            return new StockMovementResponse(
                    movement.getId().getValue(),
                    movement.getInventoryItemId().getValue(),
                    movement.getType().name(),
                    movement.getQuantity(),
                    movement.getQuantityBefore(),
                    movement.getQuantityAfter(),
                    movement.getOnHandBefore(),
                    movement.getOnHandAfter(),
                    movement.getReservedBefore(),
                    movement.getReservedAfter(),
                    movement.getReferenceId(),
                    movement.getCreatedAt()
            );
        }
    }
}
