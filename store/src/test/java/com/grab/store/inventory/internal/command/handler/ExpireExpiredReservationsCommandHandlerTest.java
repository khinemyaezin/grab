package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.entity.InventoryReservation;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.enums.InventoryReservationStatus;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.InventoryReservationRepository;
import com.inventory.domain.repository.StockMovementRepository;
import com.grab.store.inventory.internal.command.ExpireExpiredReservationsCommand;
import com.grab.store.inventory.internal.command.ExpireExpiredReservationsResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpireExpiredReservationsCommandHandlerTest {

    @Mock
    private InventoryReservationRepository inventoryReservationRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private StockMovementRepository stockMovementRepository;
    @Mock
    private IdGenerator idGenerator;

    private ExpireExpiredReservationsCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ExpireExpiredReservationsCommandHandler(
                inventoryReservationRepository,
                inventoryRepository,
                stockMovementRepository,
                idGenerator
        );
    }

    @Test
    void handle_shouldReleaseStockAndMarkExpired() {
        Id reservationId = mock(Id.class);
        Id itemId = mock(Id.class);
        Id actorId = mock(Id.class);
        Id movementId = mock(Id.class);
        when(idGenerator.generateId()).thenReturn(movementId);

        InventoryReservation reservation = InventoryReservation.create(
                reservationId, itemId, "ord-1", "line-1", 3, LocalDateTime.now().minusMinutes(1), null);
        InventoryItem item = mock(InventoryItem.class);
        StockMovement movement = mock(StockMovement.class);
        when(item.releaseReservation(eq(3), eq("ord-1"), eq(actorId), eq(movementId))).thenReturn(movement);

        LocalDateTime asOf = LocalDateTime.now();
        when(inventoryReservationRepository.findExpiredActive(asOf, 50)).thenReturn(List.of(reservation));
        when(inventoryRepository.findById(itemId)).thenReturn(Optional.of(item));

        ExpireExpiredReservationsResult result = handler.handle(
                new ExpireExpiredReservationsCommand(asOf, 50, actorId));

        assertThat(result.scanned()).isEqualTo(1);
        assertThat(result.expired()).isEqualTo(1);
        assertThat(reservation.getStatus()).isEqualTo(InventoryReservationStatus.EXPIRED);
        verify(inventoryRepository).save(item);
        verify(stockMovementRepository).save(movement);
        verify(inventoryReservationRepository).save(reservation);
    }

    @Test
    void handle_missingItem_shouldStillExpireReservation() {
        Id reservationId = mock(Id.class);
        Id itemId = mock(Id.class);
        Id actorId = mock(Id.class);
        InventoryReservation reservation = InventoryReservation.create(
                reservationId, itemId, "ord-1", "line-1", 1, LocalDateTime.now().minusMinutes(1), null);

        LocalDateTime asOf = LocalDateTime.now();
        when(inventoryReservationRepository.findExpiredActive(asOf, 10)).thenReturn(List.of(reservation));
        when(inventoryRepository.findById(itemId)).thenReturn(Optional.empty());

        ExpireExpiredReservationsResult result = handler.handle(
                new ExpireExpiredReservationsCommand(asOf, 10, actorId));

        assertThat(result.expired()).isEqualTo(1);
        assertThat(reservation.getStatus()).isEqualTo(InventoryReservationStatus.EXPIRED);
        ArgumentCaptor<InventoryReservation> captor = ArgumentCaptor.forClass(InventoryReservation.class);
        verify(inventoryReservationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InventoryReservationStatus.EXPIRED);
    }
}
