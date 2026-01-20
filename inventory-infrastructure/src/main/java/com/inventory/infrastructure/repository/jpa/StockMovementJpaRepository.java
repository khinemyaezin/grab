package com.inventory.infrastructure.repository.jpa;

import com.inventory.domain.enums.StockMovementType;
import com.inventory.infrastructure.entity.StockMovementEntity;
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

    List<StockMovementEntity> findAllByInventoryItemId(Long inventoryItemId);

    List<StockMovementEntity> findAllByReferenceId(String referenceId);

    List<StockMovementEntity> findAllByType(StockMovementType type);

    @Query("SELECT m FROM StockMovementEntity m WHERE m.inventoryItem.id = :inventoryItemId " +
            "AND m.createdAt BETWEEN :startDate AND :endDate ORDER BY m.createdAt DESC")
    List<StockMovementEntity> findByInventoryItemIdAndDateRange(
            @Param("inventoryItemId") Long inventoryItemId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    Page<StockMovementEntity> findAllByInventoryItemIdOrderByCreatedAtDesc(Long inventoryItemId, Pageable pageable);
}
