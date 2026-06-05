package com.inventory.infrastructure.repository.jpa;

import com.inventory.domain.enums.StockMovementType;
import com.inventory.infrastructure.entity.StockMovementEntity;
import com.inventory.infrastructure.view.StockMovementView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockMovementJpaRepository extends JpaRepository<StockMovementEntity, Long> {

    Optional<StockMovementEntity> findByUuid(String uuid);

    Page<StockMovementView> findAllByInventoryItemUuid(String inventoryItemUuid, Pageable pageable);

    Page<StockMovementView> findAllByReferenceId(String referenceId, Pageable pageable);

    Page<StockMovementView> findAllByType(StockMovementType type, Pageable pageable);

    @Query(value = """
                SELECT new com.inventory.infrastructure.view.StockMovementView(
                        m.uuid,
                        m.inventoryItemUuid,
                        m.type,
                        m.quantity,
                        m.quantityBefore,
                        m.quantityAfter,
                        m.onHandBefore,
                        m.onHandAfter,
                        m.reservedBefore,
                        m.reservedAfter,
                        m.referenceId,
                        m.createdAt,
                        m.createdBy
                    ) FROM StockMovementEntity m WHERE m.inventoryItemUuid = :inventoryItemUuid
                AND m.createdAt BETWEEN :startDate AND :endDate
            """,
            countQuery = """
                        SELECT count(m) FROM StockMovementEntity m
                        WHERE m.inventoryItemUuid = :inventoryItemUuid
                        AND m.createdAt BETWEEN :startDate AND :endDate
                    """)
    Page<StockMovementView> findByInventoryItemUuidAndDateRange(
            @Param("inventoryItemUuid") String inventoryItemUuid,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    Page<StockMovementView> findAllByInventoryItemUuidOrderByCreatedAtDesc(String inventoryItemUuid, Pageable pageable);

    int countByInventoryItemUuidAndType(String inventoryItemUuid, StockMovementType type);

    @Query("""
            SELECT new com.inventory.infrastructure.view.StockMovementView(
                        m.uuid,
                        m.inventoryItemUuid,
                        m.type,
                        m.quantity,
                        m.quantityBefore,
                        m.quantityAfter,
                        m.onHandBefore,
                        m.onHandAfter,
                        m.reservedBefore,
                        m.reservedAfter,
                        m.referenceId,
                        m.createdAt,
                        m.createdBy
                    ) FROM StockMovementEntity m WHERE m.createdAt >= :since
            """)
    Page<StockMovementView> findRecentMovements(@Param("since") LocalDateTime since, Pageable pageable);
}
