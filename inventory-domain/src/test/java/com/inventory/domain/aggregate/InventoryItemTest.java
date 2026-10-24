package com.inventory.domain.aggregate;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.enums.AdjustmentReason;
import com.inventory.domain.enums.InventoryStatus;
import com.inventory.domain.enums.StockMovementType;
import com.inventory.domain.event.*;
import com.inventory.domain.exception.InventoryDomainError;
import com.inventory.domain.exception.InventoryDomainValidationException;
import com.inventory.domain.valueobject.InventoryQuantity;
import com.inventory.domain.valueobject.ReorderConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryItemTest {

    private static final String SKU = "SKU-001";
    private static final Id ID = id("item-1");
    private static final Id MERCHANT_ID = id("seller-1");
    private static final Id PRODUCT_VARIANT_ID = id("variant-1");
    private static final Id LOCATION_ID = id("location-1");
    private static final Id USER_ID = id("user-1");
    private static final Id MOVEMENT_ID = id("movement-1");

    private InventoryItem item;

    @BeforeEach
    void setUp() {
        item = InventoryItem.create(ID, SKU, MERCHANT_ID, PRODUCT_VARIANT_ID, LOCATION_ID, 100, ReorderConfig.defaultConfig());
    }

    @Nested
    @DisplayName("PRD 7.1: Inventory Records")
    class InventoryRecordsTests {

        @Test
        void create_requiredFields_shouldInitializeWithRequiredFields() {
            assertThat(item.getId()).isEqualTo(ID);
            assertThat(item.getSku()).isEqualTo(SKU);
            assertThat(item.getMerchantId()).isEqualTo(MERCHANT_ID);
            assertThat(item.getProductVariantId()).isEqualTo(PRODUCT_VARIANT_ID);
            assertThat(item.getLocationId()).isEqualTo(LOCATION_ID);
            assertThat(item.getStatus()).isEqualTo(InventoryStatus.ACTIVE);
        }

        @Test
        void create_withInitialQuantity_shouldSetOnHand() {
            assertThat(item.getQuantity().onHand()).isEqualTo(100);
            assertThat(item.getAvailableQuantity()).isEqualTo(100);
        }

        @Test
        void create_withZeroInitialQuantity_shouldNotEmitEvent() {
            InventoryItem zeroItem = InventoryItem.create(ID, SKU, MERCHANT_ID, PRODUCT_VARIANT_ID, LOCATION_ID, 0, ReorderConfig.defaultConfig());

            assertThat(zeroItem.getEvents()).isEmpty();
        }

        @Test
        void create_withPositiveInitialQuantity_shouldEmitStockReceivedEvent() {
            List<Event> events = item.getEvents();

            assertThat(events).hasSize(1);
            assertThat(events.getFirst()).isInstanceOf(StockReceivedEvent.class);
            StockReceivedEvent event = (StockReceivedEvent) events.getFirst();
            assertThat(event.sku()).isEqualTo(SKU);
            assertThat(event.quantity()).isEqualTo(100);
        }

        @Test
        void constructor_withNullSku_shouldThrow() {
            assertThatThrownBy(() -> new InventoryItem(ID, null, MERCHANT_ID, PRODUCT_VARIANT_ID, LOCATION_ID, null, null, null, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("sku is required");
        }

        @Test
        void constructor_withNullSellerId_shouldThrow() {
            assertThatThrownBy(() -> new InventoryItem(ID, SKU, null, PRODUCT_VARIANT_ID, LOCATION_ID, null, null, null, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("merchantId is required");
        }

        @Test
        void constructor_withNullLocationId_shouldThrow() {
            assertThatThrownBy(() -> new InventoryItem(ID, SKU, MERCHANT_ID, PRODUCT_VARIANT_ID, null, null, null, null, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("locationId is required");
        }
    }

    @Nested
    @DisplayName("PRD 7.2: Stock Availability")
    class StockAvailabilityTests {

        @Test
        void getAvailableQuantity_reservedAndDamagedStock_shouldCalculateOnHandMinusReservedMinusDamaged() {
            item.reserveStock(20, "order-1", USER_ID, MOVEMENT_ID);
            item.markDamaged(10, "damaged", USER_ID, id("mov-2"));

            assertThat(item.getAvailableQuantity()).isEqualTo(70);
        }

        @Test
        void getAvailableQuantity_deductionsExceedingOnHand_shouldNeverBeNegative() {
            InventoryQuantity quantity = new InventoryQuantity(10, 15, 0, 5);
            InventoryItem testItem = new InventoryItem(ID, SKU, MERCHANT_ID, PRODUCT_VARIANT_ID, LOCATION_ID, quantity, null, InventoryStatus.ACTIVE, LocalDateTime.now());

            assertThat(testItem.getAvailableQuantity()).isZero();
        }

        @Test
        void quantity_mixedStockMovements_shouldTrackAllDimensions() {
            item.reserveStock(10, "order-1", USER_ID, MOVEMENT_ID);

            assertThat(item.getQuantity().onHand()).isEqualTo(100);
            assertThat(item.getQuantity().reserved()).isEqualTo(10);
            assertThat(item.getQuantity().inTransit()).isZero();
            assertThat(item.getQuantity().damaged()).isZero();
        }
    }

    @Nested
    @DisplayName("PRD 7.3: Reservation Management")
    class ReservationManagementTests {

        @Test
        void reserveStock_sufficientAvailableStock_shouldReduceAvailableQuantity() {
            StockMovement movement = item.reserveStock(30, "order-1", USER_ID, MOVEMENT_ID);

            assertThat(item.getAvailableQuantity()).isEqualTo(70);
            assertThat(item.getQuantity().reserved()).isEqualTo(30);
            assertThat(movement).isNotNull();
            assertThat(movement.getType()).isEqualTo(StockMovementType.RESERVATION);
        }

        @Test
        void reserveStock_validReservation_shouldEmitStockReservedEvent() {
            item.reserveStock(30, "order-1", USER_ID, MOVEMENT_ID);

            List<Event> events = item.getEvents();
            assertThat(events).hasSize(2);
            assertThat(events.get(1)).isInstanceOf(StockReservedEvent.class);
            StockReservedEvent event = (StockReservedEvent) events.get(1);
            assertThat(event.orderId()).isEqualTo("order-1");
            assertThat(event.quantity()).isEqualTo(30);
        }

        @Test
        void reserveStock_withInsufficientAvailable_shouldThrow() {
            assertThatThrownBy(() -> item.reserveStock(150, "order-1", USER_ID, MOVEMENT_ID))
                    .isInstanceOf(InventoryDomainValidationException.class)
                    .satisfies(ex -> {
                        InventoryDomainValidationException validationEx = (InventoryDomainValidationException) ex;
                        assertThat(validationEx.getMessageSource()).isInstanceOf(InventoryDomainError.InsufficientQuantity.class);
                    });
        }

        @Test
        void reserveStock_withNegativeQuantity_shouldThrow() {
            assertThatThrownBy(() -> item.reserveStock(-5, "order-1", USER_ID, MOVEMENT_ID))
                    .isInstanceOf(InventoryDomainValidationException.class)
                    .satisfies(ex -> {
                        InventoryDomainValidationException validationEx = (InventoryDomainValidationException) ex;
                        assertThat(validationEx.getMessageSource()).isInstanceOf(InventoryDomainError.NegativeQuantity.class);
                    });
        }

        @Test
        void releaseReservation_validReservedQuantity_shouldReturnStockToAvailable() {
            item.reserveStock(30, "order-1", USER_ID, MOVEMENT_ID);
            StockMovement movement = item.releaseReservation(20, "order-1", USER_ID, id("mov-2"));

            assertThat(item.getAvailableQuantity()).isEqualTo(90);
            assertThat(item.getQuantity().reserved()).isEqualTo(10);
            assertThat(movement.getType()).isEqualTo(StockMovementType.RESERVATION_RELEASE);
        }

        @Test
        void releaseReservation_withMoreThanReserved_shouldThrow() {
            item.reserveStock(30, "order-1", USER_ID, MOVEMENT_ID);

            assertThatThrownBy(() -> item.releaseReservation(50, "order-1", USER_ID, id("mov-2")))
                    .isInstanceOf(InventoryDomainValidationException.class);
        }

        @Test
        void shipStock_reservedStock_shouldDeductFromReservedAndOnHand() {
            item.reserveStock(30, "order-1", USER_ID, MOVEMENT_ID);
            StockMovement movement = item.shipStock(20, "order-1", USER_ID, id("mov-2"));

            assertThat(item.getQuantity().onHand()).isEqualTo(80);
            assertThat(item.getQuantity().reserved()).isEqualTo(10);
            assertThat(item.getAvailableQuantity()).isEqualTo(70);
            assertThat(movement.getType()).isEqualTo(StockMovementType.SALE);
        }

        @Test
        void shipStock_validShipment_shouldEmitStockShippedEvent() {
            item.reserveStock(30, "order-1", USER_ID, MOVEMENT_ID);
            item.shipStock(20, "order-1", USER_ID, id("mov-2"));

            List<Event> events = item.getEvents();
            assertThat(events).hasSize(3);
            assertThat(events.get(2)).isInstanceOf(StockShippedEvent.class);
        }

        @Test
        void shipStock_withMoreThanReserved_shouldThrow() {
            item.reserveStock(30, "order-1", USER_ID, MOVEMENT_ID);

            assertThatThrownBy(() -> item.shipStock(50, "order-1", USER_ID, id("mov-2")))
                    .isInstanceOf(InventoryDomainValidationException.class);
        }
    }

    @Nested
    @DisplayName("PRD 7.4: Stock Operations")
    class StockOperationsTests {

        @Test
        void receiveStock_withPurchaseOrderReceipt_shouldIncreaseOnHand() {
            StockMovement movement = item.receiveStock(50, StockMovementType.PURCHASE_ORDER_RECEIPT, "PO-123", "notes", USER_ID, MOVEMENT_ID);

            assertThat(item.getQuantity().onHand()).isEqualTo(150);
            assertThat(item.getAvailableQuantity()).isEqualTo(150);
            assertThat(movement.getType()).isEqualTo(StockMovementType.PURCHASE_ORDER_RECEIPT);
        }

        @Test
        void receiveStock_withCustomerReturn_shouldIncreaseOnHand() {
            StockMovement movement = item.receiveStock(20, StockMovementType.CUSTOMER_RETURN, "RET-123", "notes", USER_ID, MOVEMENT_ID);

            assertThat(item.getQuantity().onHand()).isEqualTo(120);
            assertThat(movement.getType()).isEqualTo(StockMovementType.CUSTOMER_RETURN);
        }

        @Test
        void receiveStock_withTransferIn_shouldIncreaseOnHand() {
            StockMovement movement = item.receiveStock(30, StockMovementType.TRANSFER_IN, "TRF-123", "notes", USER_ID, MOVEMENT_ID);

            assertThat(item.getQuantity().onHand()).isEqualTo(130);
            assertThat(movement.getType()).isEqualTo(StockMovementType.TRANSFER_IN);
        }

        @Test
        void receiveStock_withInvalidType_shouldThrow() {
            assertThatThrownBy(() -> item.receiveStock(50, StockMovementType.SALE, "REF-123", "notes", USER_ID, MOVEMENT_ID))
                    .isInstanceOf(InventoryDomainValidationException.class)
                    .satisfies(ex -> {
                        InventoryDomainValidationException validationEx = (InventoryDomainValidationException) ex;
                        assertThat(validationEx.getMessageSource()).isInstanceOf(InventoryDomainError.InvalidStockMovementType.class);
                    });
        }

        @Test
        void receiveStock_validReceipt_shouldEmitStockReceivedEvent() {
            item.receiveStock(50, StockMovementType.PURCHASE_ORDER_RECEIPT, "PO-123", "notes", USER_ID, MOVEMENT_ID);

            List<Event> events = item.getEvents();
            assertThat(events).hasSize(2);
            assertThat(events.get(1)).isInstanceOf(StockReceivedEvent.class);
        }

        @Test
        void adjustStock_validAbsoluteQuantity_shouldSetAbsoluteOnHandQuantity() {
            StockMovement movement = item.adjustStock(80, AdjustmentReason.CYCLE_COUNT, "notes", USER_ID, MOVEMENT_ID);

            assertThat(item.getQuantity().onHand()).isEqualTo(80);
            assertThat(movement.getType()).isEqualTo(StockMovementType.CYCLE_COUNT_ADJUSTMENT);
        }

        @Test
        void adjustStock_validAdjustment_shouldEmitStockAdjustedEvent() {
            item.adjustStock(80, AdjustmentReason.CYCLE_COUNT, "notes", USER_ID, MOVEMENT_ID);

            List<Event> events = item.getEvents();
            assertThat(events).hasSize(2);
            assertThat(events.get(1)).isInstanceOf(StockAdjustedEvent.class);
            StockAdjustedEvent event = (StockAdjustedEvent) events.get(1);
            assertThat(event.previousQuantity()).isEqualTo(100);
            assertThat(event.newQuantity()).isEqualTo(80);
        }

        @Test
        void markDamaged_availableStock_shouldNotSubtractWithOnHandQty() {
            StockMovement movement = item.markDamaged(15, "notes", USER_ID, MOVEMENT_ID);

            assertThat(item.getQuantity().onHand()).isEqualTo(100);
            assertThat(item.getQuantity().damaged()).isEqualTo(15);
            assertThat(item.getAvailableQuantity()).isEqualTo(85);
            assertThat(movement.getType()).isEqualTo(StockMovementType.DAMAGE_ADJUSTMENT);
        }

        @Test
        void markDamaged_validDamageQuantity_shouldEmitStockAdjustedEvent() {
            item.markDamaged(15, "notes", USER_ID, MOVEMENT_ID);

            List<Event> events = item.getEvents();
            assertThat(events).hasSize(2);
            assertThat(events.get(1)).isInstanceOf(StockAdjustedEvent.class);
            StockAdjustedEvent event = (StockAdjustedEvent) events.get(1);
            assertThat(event.reason()).isEqualTo(AdjustmentReason.DAMAGED);
        }

        @Test
        void writeOff_availableStock_shouldReduceOnHand() {
            StockMovement movement = item.writeOff(25, "EXPIRED", "notes", USER_ID, MOVEMENT_ID);

            assertThat(item.getQuantity().onHand()).isEqualTo(75);
            assertThat(item.getAvailableQuantity()).isEqualTo(75);
            assertThat(movement.getType()).isEqualTo(StockMovementType.WRITE_OFF);
        }

        @Test
        void writeOff_withMoreThanAvailable_shouldThrow() {
            assertThatThrownBy(() -> item.writeOff(150, "LOST", "notes", USER_ID, MOVEMENT_ID))
                    .isInstanceOf(InventoryDomainValidationException.class);
        }

        @Test
        void transferOut_availableStock_shouldReduceOnHand() {
            StockMovement movement = item.transferOut(30, "TRF-456", USER_ID, MOVEMENT_ID);

            assertThat(item.getQuantity().onHand()).isEqualTo(70);
            assertThat(item.getAvailableQuantity()).isEqualTo(70);
            assertThat(movement.getType()).isEqualTo(StockMovementType.TRANSFER_OUT);
        }

        @Test
        void transferOut_withMoreThanAvailable_shouldThrow() {
            assertThatThrownBy(() -> item.transferOut(150, "TRF-456", USER_ID, MOVEMENT_ID))
                    .isInstanceOf(InventoryDomainValidationException.class);
        }

        @Test
        void returnToVendor_availableStock_shouldReduceOnHand() {
            StockMovement movement = item.returnToVendor(20, "DEFECTIVE", "notes", USER_ID, MOVEMENT_ID);

            assertThat(item.getQuantity().onHand()).isEqualTo(80);
            assertThat(item.getAvailableQuantity()).isEqualTo(80);
            assertThat(movement.getType()).isEqualTo(StockMovementType.RETURN_TO_VENDOR);
        }

        @Test
        void returnToVendor_withMoreThanAvailable_shouldThrow() {
            assertThatThrownBy(() -> item.returnToVendor(150, "DEFECTIVE", "notes", USER_ID, MOVEMENT_ID))
                    .isInstanceOf(InventoryDomainValidationException.class);
        }

        @Test
        void allStockOperations_validQuantities_shouldReturnStockMovement() {
            StockMovement receive = item.receiveStock(10, StockMovementType.PURCHASE_ORDER_RECEIPT, "ref", "notes", USER_ID, id("mov-1"));
            StockMovement reserve = item.reserveStock(5, "order-1", USER_ID, id("mov-2"));
            StockMovement release = item.releaseReservation(3, "order-1", USER_ID, id("mov-3"));
            StockMovement ship = item.shipStock(2, "order-1", USER_ID, id("mov-4"));
            StockMovement adjust = item.adjustStock(90, AdjustmentReason.CYCLE_COUNT, "notes", USER_ID, id("mov-5"));
            StockMovement damage = item.markDamaged(5, "notes", USER_ID, id("mov-6"));
            StockMovement writeOff = item.writeOff(10, "LOST", "notes", USER_ID, id("mov-7"));
            StockMovement transfer = item.transferOut(10, "TRF-1", USER_ID, id("mov-8"));
            StockMovement returnToVendor = item.returnToVendor(10, "DEFECTIVE", "notes", USER_ID, id("mov-9"));

            assertThat(receive).isNotNull();
            assertThat(reserve).isNotNull();
            assertThat(release).isNotNull();
            assertThat(ship).isNotNull();
            assertThat(adjust).isNotNull();
            assertThat(damage).isNotNull();
            assertThat(writeOff).isNotNull();
            assertThat(transfer).isNotNull();
            assertThat(returnToVendor).isNotNull();
        }
    }

    @Nested
    @DisplayName("PRD 7.7: Oversell Prevention")
    class OversellPreventionTests {

        @Test
        void reserveStock_excessQuantity_shouldPreventOverselling() {
            item.reserveStock(80, "order-1", USER_ID, MOVEMENT_ID);

            assertThatThrownBy(() -> item.reserveStock(30, "order-2", USER_ID, id("mov-2")))
                    .isInstanceOf(InventoryDomainValidationException.class);
        }

        @Test
        void writeOff_excessQuantity_shouldPreventGoingBelowZero() {
            assertThatThrownBy(() -> item.writeOff(150, "LOST", "notes", USER_ID, MOVEMENT_ID))
                    .isInstanceOf(InventoryDomainValidationException.class);
        }

        @Test
        void shipStock_excessReservedQuantity_shouldPreventShippingMoreThanReserved() {
            item.reserveStock(30, "order-1", USER_ID, MOVEMENT_ID);

            assertThatThrownBy(() -> item.shipStock(50, "order-1", USER_ID, id("mov-2")))
                    .isInstanceOf(InventoryDomainValidationException.class);
        }

        @Test
        void transferOut_excessQuantity_shouldPreventGoingBelowZero() {
            assertThatThrownBy(() -> item.transferOut(150, "TRF-1", USER_ID, MOVEMENT_ID))
                    .isInstanceOf(InventoryDomainValidationException.class);
        }

        @Test
        void returnToVendor_excessQuantity_shouldPreventGoingBelowZero() {
            assertThatThrownBy(() -> item.returnToVendor(150, "DEFECTIVE", "notes", USER_ID, MOVEMENT_ID))
                    .isInstanceOf(InventoryDomainValidationException.class);
        }
    }

    @Nested
    @DisplayName("PRD 7.8: Inventory Status Lifecycle")
    class StatusLifecycleTests {

        @Test
        void status_whenAvailableReachesZero_shouldAutoTransitionToOutOfStock() {
            item.writeOff(100, "LOST", "notes", USER_ID, MOVEMENT_ID);

            assertThat(item.getStatus()).isEqualTo(InventoryStatus.OUT_OF_STOCK);
        }

        @Test
        void status_whenStockReplenished_shouldAutoTransitionToActive() {
            item.writeOff(100, "LOST", "notes", USER_ID, MOVEMENT_ID);
            assertThat(item.getStatus()).isEqualTo(InventoryStatus.OUT_OF_STOCK);

            item.receiveStock(50, StockMovementType.PURCHASE_ORDER_RECEIPT, "PO-1", "notes", USER_ID, id("mov-2"));

            assertThat(item.getStatus()).isEqualTo(InventoryStatus.ACTIVE);
        }

        @Test
        void suspend_activeItem_shouldSetStatusToSuspended() {
            item.suspend();

            assertThat(item.getStatus()).isEqualTo(InventoryStatus.SUSPENDED);
        }

        @Test
        void discontinue_activeItem_shouldSetStatusToDiscontinued() {
            item.discontinue();

            assertThat(item.getStatus()).isEqualTo(InventoryStatus.DISCONTINUED);
        }

        @Test
        void discontinue_activeItem_shouldEmitInventoryItemStatusChangedEvent() {
            item.discontinue();

            List<Event> events = item.getEvents();
            assertThat(events).hasSize(2);
            assertThat(events.get(1)).isInstanceOf(InventoryItemStatusChangedEvent.class);
        }

        @Test
        void activate_suspendedItemWithStock_shouldSetStatusToActive() {
            item.suspend();
            item.activate();

            assertThat(item.getStatus()).isEqualTo(InventoryStatus.ACTIVE);
        }

        @Test
        void activate_withZeroStock_shouldSetStatusToOutOfStock() {
            item.writeOff(100, "LOST", "notes", USER_ID, MOVEMENT_ID);
            item.suspend();
            item.activate();

            assertThat(item.getStatus()).isEqualTo(InventoryStatus.OUT_OF_STOCK);
        }

        @Test
        void receiveStock_onSuspendedItem_shouldThrow() {
            item.suspend();

            assertThatThrownBy(() -> item.receiveStock(50, StockMovementType.PURCHASE_ORDER_RECEIPT, "PO-1", "notes", USER_ID, MOVEMENT_ID))
                    .isInstanceOf(InventoryDomainValidationException.class)
                    .satisfies(ex -> {
                        InventoryDomainValidationException validationEx = (InventoryDomainValidationException) ex;
                        assertThat(validationEx.getMessageSource()).isInstanceOf(InventoryDomainError.StockOperationBlocked.class);
                    });
        }

        @Test
        void reserveStock_onSuspendedItem_shouldThrow() {
            item.suspend();

            assertThatThrownBy(() -> item.reserveStock(10, "order-1", USER_ID, MOVEMENT_ID))
                    .isInstanceOf(InventoryDomainValidationException.class);
        }

        @Test
        void adjustStock_onSuspendedItem_shouldThrow() {
            item.suspend();

            assertThatThrownBy(() -> item.adjustStock(80, AdjustmentReason.CYCLE_COUNT, "notes", USER_ID, MOVEMENT_ID))
                    .isInstanceOf(InventoryDomainValidationException.class);
        }

        @Test
        void markDamaged_onSuspendedItem_shouldThrow() {
            item.suspend();

            assertThatThrownBy(() -> item.markDamaged(10, "notes", USER_ID, MOVEMENT_ID))
                    .isInstanceOf(InventoryDomainValidationException.class);
        }

        @Test
        void writeOff_onSuspendedItem_shouldThrow() {
            item.suspend();

            assertThatThrownBy(() -> item.writeOff(10, "LOST", "notes", USER_ID, MOVEMENT_ID))
                    .isInstanceOf(InventoryDomainValidationException.class);
        }

        @Test
        void transferOut_onSuspendedItem_shouldThrow() {
            item.suspend();

            assertThatThrownBy(() -> item.transferOut(10, "TRF-1", USER_ID, MOVEMENT_ID))
                    .isInstanceOf(InventoryDomainValidationException.class);
        }

        @Test
        void returnToVendor_onSuspendedItem_shouldThrow() {
            item.suspend();

            assertThatThrownBy(() -> item.returnToVendor(10, "DEFECTIVE", "notes", USER_ID, MOVEMENT_ID))
                    .isInstanceOf(InventoryDomainValidationException.class);
        }

        @Test
        void receiveStock_onDiscontinuedItem_shouldThrow() {
            item.discontinue();

            assertThatThrownBy(() -> item.receiveStock(50, StockMovementType.PURCHASE_ORDER_RECEIPT, "PO-1", "notes", USER_ID, MOVEMENT_ID))
                    .isInstanceOf(InventoryDomainValidationException.class);
        }

        @Test
        void reserveStock_onDiscontinuedItem_shouldThrow() {
            item.discontinue();

            assertThatThrownBy(() -> item.reserveStock(10, "order-1", USER_ID, MOVEMENT_ID))
                    .isInstanceOf(InventoryDomainValidationException.class);
        }

        @Test
        void shipStock_onDiscontinuedItem_shouldThrow() {
            item.discontinue();

            assertThatThrownBy(() -> item.shipStock(10, "order-1", USER_ID, MOVEMENT_ID))
                    .isInstanceOf(InventoryDomainValidationException.class);
        }

        @Test
        void releaseReservation_onDiscontinuedItem_shouldThrow() {
            item.discontinue();

            assertThatThrownBy(() -> item.releaseReservation(10, "order-1", USER_ID, MOVEMENT_ID))
                    .isInstanceOf(InventoryDomainValidationException.class);
        }

        @Test
        void markDamaged_onDiscontinuedItem_shouldThrow() {
            item.discontinue();

            assertThatThrownBy(() -> item.markDamaged(10, "notes", USER_ID, MOVEMENT_ID))
                    .isInstanceOf(InventoryDomainValidationException.class);
        }

        @Test
        void markDamaged_whenOnHandReachesZero_shouldAutoTransitionToOutOfStock() {
            item.markDamaged(100, "notes", USER_ID, MOVEMENT_ID);

            assertThat(item.getStatus()).isEqualTo(InventoryStatus.OUT_OF_STOCK);
        }
    }

    @Nested
    @DisplayName("PRD 7.10: Reorder Service")
    class ReorderServiceTests {

        @Test
        void isLowStock_stockBelowSafetyThreshold_shouldReturnTrueWhenBelowSafetyStock() {
            ReorderConfig config = new ReorderConfig(10, 30, 50, 200);
            InventoryItem lowStockItem = InventoryItem.create(ID, SKU, MERCHANT_ID, PRODUCT_VARIANT_ID, LOCATION_ID, 10, config);

            assertThat(lowStockItem.isLowStock()).isTrue();
        }

        @Test
        void isLowStock_stockAboveSafetyThreshold_shouldReturnFalseWhenAboveSafetyStock() {
            ReorderConfig config = new ReorderConfig(10, 30, 50, 200);
            InventoryItem normalItem = InventoryItem.create(ID, SKU, MERCHANT_ID, PRODUCT_VARIANT_ID, LOCATION_ID, 50, config);

            assertThat(normalItem.isLowStock()).isFalse();
        }

        @Test
        void needsReorder_stockBelowReorderPoint_shouldReturnTrueWhenBelowReorderPoint() {
            ReorderConfig config = new ReorderConfig(10, 30, 50, 200);
            InventoryItem lowStockItem = InventoryItem.create(ID, SKU, MERCHANT_ID, PRODUCT_VARIANT_ID, LOCATION_ID, 25, config);

            assertThat(lowStockItem.needsReorder()).isTrue();
        }

        @Test
        void getSuggestedReorderQuantity_stockBelowReorderPoint_shouldReturnConfiguredQuantity() {
            ReorderConfig config = new ReorderConfig(10, 30, 50, 200);
            InventoryItem lowStockItem = InventoryItem.create(ID, SKU, MERCHANT_ID, PRODUCT_VARIANT_ID, LOCATION_ID, 25, config);

            assertThat(lowStockItem.getSuggestedReorderQuantity()).isEqualTo(50);
        }

        @Test
        void getSuggestedReorderQuantity_reorderExceedingMaxStock_shouldCapAtMaxStock() {
            ReorderConfig config = new ReorderConfig(10, 30, 50, 40);
            InventoryItem lowStockItem = InventoryItem.create(ID, SKU, MERCHANT_ID, PRODUCT_VARIANT_ID, LOCATION_ID, 25, config);

            assertThat(lowStockItem.getSuggestedReorderQuantity()).isEqualTo(15);
        }

        @Test
        void create_whenInitialQuantityExceedsMaxStock_shouldReject() {
            ReorderConfig config = new ReorderConfig(10, 30, 50, 80);

            assertThatThrownBy(() -> InventoryItem.create(ID, SKU, MERCHANT_ID, PRODUCT_VARIANT_ID, LOCATION_ID, 100, config))
                    .isInstanceOf(InventoryDomainValidationException.class)
                    .satisfies(ex -> {
                        InventoryDomainValidationException validationEx = (InventoryDomainValidationException) ex;
                        assertThat(validationEx.getMessageSource()).isInstanceOf(InventoryDomainError.ExceedsMaxStock.class);
                    });
        }

        @Test
        void receiveStock_whenExceedingMaxStock_shouldReject() {
            ReorderConfig config = new ReorderConfig(10, 30, 50, 120);
            InventoryItem cappedItem = InventoryItem.create(ID, SKU, MERCHANT_ID, PRODUCT_VARIANT_ID, LOCATION_ID, 100, config);

            assertThatThrownBy(() -> cappedItem.receiveStock(30, StockMovementType.PURCHASE_ORDER_RECEIPT, "PO-1", "notes", USER_ID, MOVEMENT_ID))
                    .isInstanceOf(InventoryDomainValidationException.class)
                    .satisfies(ex -> {
                        InventoryDomainValidationException validationEx = (InventoryDomainValidationException) ex;
                        assertThat(validationEx.getMessageSource()).isInstanceOf(InventoryDomainError.ExceedsMaxStock.class);
                    });
            assertThat(cappedItem.getQuantity().onHand()).isEqualTo(100);
        }

        @Test
        void receiveStock_whenWithinMaxStock_shouldAllow() {
            ReorderConfig config = new ReorderConfig(10, 30, 50, 120);
            InventoryItem cappedItem = InventoryItem.create(ID, SKU, MERCHANT_ID, PRODUCT_VARIANT_ID, LOCATION_ID, 100, config);

            cappedItem.receiveStock(20, StockMovementType.PURCHASE_ORDER_RECEIPT, "PO-1", "notes", USER_ID, MOVEMENT_ID);

            assertThat(cappedItem.getQuantity().onHand()).isEqualTo(120);
        }

        @Test
        void adjustStock_whenNewOnHandExceedsMaxStock_shouldReject() {
            ReorderConfig config = new ReorderConfig(10, 30, 50, 120);
            InventoryItem cappedItem = InventoryItem.create(ID, SKU, MERCHANT_ID, PRODUCT_VARIANT_ID, LOCATION_ID, 100, config);

            assertThatThrownBy(() -> cappedItem.adjustStock(150, AdjustmentReason.CYCLE_COUNT, "notes", USER_ID, MOVEMENT_ID))
                    .isInstanceOf(InventoryDomainValidationException.class)
                    .satisfies(ex -> {
                        InventoryDomainValidationException validationEx = (InventoryDomainValidationException) ex;
                        assertThat(validationEx.getMessageSource()).isInstanceOf(InventoryDomainError.ExceedsMaxStock.class);
                    });
        }

        @Test
        void receiveStock_receiptBelowThreshold_shouldEmitLowStockAlertWhenBelowThreshold() {
            ReorderConfig config = new ReorderConfig(10, 30, 50, 200);
            InventoryItem testItem = InventoryItem.create(ID, SKU, MERCHANT_ID, PRODUCT_VARIANT_ID, LOCATION_ID, 50, config);
            testItem.pullEvents();

            testItem.writeOff(40, "LOST", "notes", USER_ID, MOVEMENT_ID);

            List<Event> events = testItem.getEvents();
            assertThat(events).anyMatch(e -> e instanceof LowStockAlertEvent);
        }

        @Test
        void reserveStock_reservationCrossingThreshold_shouldEmitLowStockAlertWhenBelowThreshold() {
            ReorderConfig config = new ReorderConfig(10, 30, 50, 200);
            InventoryItem testItem = InventoryItem.create(ID, SKU, MERCHANT_ID, PRODUCT_VARIANT_ID, LOCATION_ID, 50, config);
            testItem.pullEvents();

            testItem.reserveStock(40, "order-1", USER_ID, MOVEMENT_ID);

            List<Event> events = testItem.getEvents();
            assertThat(events).anyMatch(e -> e instanceof LowStockAlertEvent);
        }

        @Test
        void shipStock_shipmentBelowThreshold_shouldEmitLowStockAlertWhenBelowThreshold() {
            ReorderConfig config = new ReorderConfig(10, 30, 50, 200);
            InventoryItem testItem = InventoryItem.create(ID, SKU, MERCHANT_ID, PRODUCT_VARIANT_ID, LOCATION_ID, 50, config);
            testItem.reserveStock(40, "order-1", USER_ID, id("mov-1"));
            testItem.shipStock(30, "order-1", USER_ID, id("mov-2"));

            List<Event> events = testItem.getEvents();
            assertThat(events).anyMatch(e -> e instanceof LowStockAlertEvent);
        }

        @Test
        void updateReorderConfig_newConfiguration_shouldReplaceConfig() {
            item.updateReorderConfig(new ReorderConfig(5, 15, 40, 100));

            assertThat(item.getReorderConfig().safetyStock()).isEqualTo(5);
            assertThat(item.getReorderConfig().reorderPoint()).isEqualTo(15);
            assertThat(item.getReorderConfig().reorderQuantity()).isEqualTo(40);
            assertThat(item.getReorderConfig().maxStock()).isEqualTo(100);
        }

        @Test
        void announceAndReceiveInTransit_announcedQuantity_shouldMoveQtyToOnHand() {
            item.announceInTransit(20, "PO-9", USER_ID, id("mov-announce"));
            assertThat(item.getQuantity().inTransit()).isEqualTo(20);
            assertThat(item.getQuantity().onHand()).isEqualTo(100);

            item.receiveInTransit(15, "PO-9", USER_ID, id("mov-receive"));
            assertThat(item.getQuantity().inTransit()).isEqualTo(5);
            assertThat(item.getQuantity().onHand()).isEqualTo(115);
        }

        @Test
        void receiveInTransit_excessQuantity_shouldRejectWhenExceedsInTransit() {
            item.announceInTransit(5, "PO-9", USER_ID, MOVEMENT_ID);

            assertThatThrownBy(() -> item.receiveInTransit(6, "PO-9", USER_ID, id("mov-2")))
                    .isInstanceOf(InventoryDomainValidationException.class)
                    .satisfies(ex -> {
                        InventoryDomainValidationException validationEx = (InventoryDomainValidationException) ex;
                        assertThat(validationEx.getMessageSource()).isInstanceOf(InventoryDomainError.ReceiveExceedsInTransit.class);
                    });
        }
    }

    @Nested
    @DisplayName("PRD 7.12: Stock Movement Types")
    class StockMovementTypesTests {

        @Test
        void receiveStock_withPurchaseOrderReceipt_shouldCreateCorrectMovementType() {
            StockMovement movement = item.receiveStock(50, StockMovementType.PURCHASE_ORDER_RECEIPT, "PO-1", "notes", USER_ID, MOVEMENT_ID);

            assertThat(movement.getType()).isEqualTo(StockMovementType.PURCHASE_ORDER_RECEIPT);
        }

        @Test
        void receiveStock_withCustomerReturn_shouldCreateCorrectMovementType() {
            StockMovement movement = item.receiveStock(50, StockMovementType.CUSTOMER_RETURN, "RET-1", "notes", USER_ID, MOVEMENT_ID);

            assertThat(movement.getType()).isEqualTo(StockMovementType.CUSTOMER_RETURN);
        }

        @Test
        void receiveStock_withTransferIn_shouldCreateCorrectMovementType() {
            StockMovement movement = item.receiveStock(50, StockMovementType.TRANSFER_IN, "TRF-1", "notes", USER_ID, MOVEMENT_ID);

            assertThat(movement.getType()).isEqualTo(StockMovementType.TRANSFER_IN);
        }

        @Test
        void receiveStock_withInitialStock_shouldCreateCorrectMovementType() {
            StockMovement movement = item.receiveStock(50, StockMovementType.INITIAL_STOCK, "INIT-1", "notes", USER_ID, MOVEMENT_ID);

            assertThat(movement.getType()).isEqualTo(StockMovementType.INITIAL_STOCK);
        }

        @Test
        void reserveStock_validQuantity_shouldCreateReservationMovementType() {
            StockMovement movement = item.reserveStock(10, "order-1", USER_ID, MOVEMENT_ID);

            assertThat(movement.getType()).isEqualTo(StockMovementType.RESERVATION);
        }

        @Test
        void releaseReservation_validQuantity_shouldCreateReservationReleaseMovementType() {
            item.reserveStock(10, "order-1", USER_ID, MOVEMENT_ID);
            StockMovement movement = item.releaseReservation(5, "order-1", USER_ID, id("mov-2"));

            assertThat(movement.getType()).isEqualTo(StockMovementType.RESERVATION_RELEASE);
        }

        @Test
        void shipStock_validQuantity_shouldCreateSaleMovementType() {
            item.reserveStock(10, "order-1", USER_ID, MOVEMENT_ID);
            StockMovement movement = item.shipStock(10, "order-1", USER_ID, id("mov-2"));

            assertThat(movement.getType()).isEqualTo(StockMovementType.SALE);
        }

        @Test
        void adjustStock_validQuantity_shouldCreateCycleCountAdjustmentMovementType() {
            StockMovement movement = item.adjustStock(80, AdjustmentReason.CYCLE_COUNT, "notes", USER_ID, MOVEMENT_ID);

            assertThat(movement.getType()).isEqualTo(StockMovementType.CYCLE_COUNT_ADJUSTMENT);
        }

        @Test
        void markDamaged_validQuantity_shouldCreateDamageAdjustmentMovementType() {
            StockMovement movement = item.markDamaged(10, "notes", USER_ID, MOVEMENT_ID);

            assertThat(movement.getType()).isEqualTo(StockMovementType.DAMAGE_ADJUSTMENT);
        }

        @Test
        void writeOff_validQuantity_shouldCreateWriteOffMovementType() {
            StockMovement movement = item.writeOff(10, "LOST", "notes", USER_ID, MOVEMENT_ID);

            assertThat(movement.getType()).isEqualTo(StockMovementType.WRITE_OFF);
        }

        @Test
        void transferOut_validQuantity_shouldCreateTransferOutMovementType() {
            StockMovement movement = item.transferOut(10, "TRF-1", USER_ID, MOVEMENT_ID);

            assertThat(movement.getType()).isEqualTo(StockMovementType.TRANSFER_OUT);
        }

        @Test
        void returnToVendor_validQuantity_shouldCreateReturnToVendorMovementType() {
            StockMovement movement = item.returnToVendor(10, "DEFECTIVE", "notes", USER_ID, MOVEMENT_ID);

            assertThat(movement.getType()).isEqualTo(StockMovementType.RETURN_TO_VENDOR);
        }
    }

    @Nested
    @DisplayName("Query Methods")
    class QueryMethodsTests {

        @Test
        void isOutOfStock_zeroAvailableQuantity_shouldReturnTrueWhenAvailableIsZero() {
            item.writeOff(100, "LOST", "notes", USER_ID, MOVEMENT_ID);

            assertThat(item.isOutOfStock()).isTrue();
        }

        @Test
        void isOutOfStock_positiveAvailableQuantity_shouldReturnFalseWhenAvailableIsPositive() {
            assertThat(item.isOutOfStock()).isFalse();
        }

        @Test
        void isActive_activeStatus_shouldReturnTrueWhenStatusIsActive() {
            assertThat(item.isActive()).isTrue();
        }

        @Test
        void isActive_suspendedStatus_shouldReturnFalseWhenStatusIsSuspended() {
            item.suspend();

            assertThat(item.isActive()).isFalse();
        }

        @Test
        void canFulfill_activeItemWithSufficientStock_shouldReturnTrueWhenActiveAndSufficientStock() {
            assertThat(item.canFulfill(50)).isTrue();
        }

        @Test
        void canFulfill_insufficientStock_shouldReturnFalseWhenInsufficientStock() {
            assertThat(item.canFulfill(150)).isFalse();
        }

        @Test
        void canFulfill_inactiveItem_shouldReturnFalseWhenNotActive() {
            item.suspend();

            assertThat(item.canFulfill(50)).isFalse();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        void receiveStock_withZeroQuantity_shouldSucceed() {
            StockMovement movement = item.receiveStock(0, StockMovementType.PURCHASE_ORDER_RECEIPT, "PO-1", "notes", USER_ID, MOVEMENT_ID);

            assertThat(movement).isNotNull();
            assertThat(item.getQuantity().onHand()).isEqualTo(100);
        }

        @Test
        void reserveStock_withZeroQuantity_shouldSucceed() {
            StockMovement movement = item.reserveStock(0, "order-1", USER_ID, MOVEMENT_ID);

            assertThat(movement).isNotNull();
            assertThat(item.getQuantity().reserved()).isZero();
        }

        @Test
        void multipleOperations_mixedStockMovements_shouldMaintainConsistency() {
            item.receiveStock(50, StockMovementType.PURCHASE_ORDER_RECEIPT, "PO-1", "notes", USER_ID, id("mov-1"));
            item.reserveStock(30, "order-1", USER_ID, id("mov-2"));
            item.shipStock(20, "order-1", USER_ID, id("mov-3"));
            item.markDamaged(10, "notes", USER_ID, id("mov-4"));
            item.writeOff(5, "LOST", "notes", USER_ID, id("mov-5"));

            assertThat(item.getQuantity().onHand()).isEqualTo(125);
            assertThat(item.getQuantity().reserved()).isEqualTo(10);
            assertThat(item.getQuantity().damaged()).isEqualTo(10);
            assertThat(item.getAvailableQuantity()).isEqualTo(105);
        }

        @Test
        void lastUpdated_successiveStockOperations_shouldBeUpdatedOnEveryOperation() {
            LocalDateTime before = item.getLastUpdated();

            item.receiveStock(10, StockMovementType.PURCHASE_ORDER_RECEIPT, "PO-1", "notes", USER_ID, id("mov-1"));

            assertThat(item.getLastUpdated()).isAfterOrEqualTo(before);
        }
    }



    private static Id id(String value) {
        return () -> value;
    }
}
