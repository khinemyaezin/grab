package com.grab.discovery.adapter.persistence.repository.jpa;

import com.grab.discovery.adapter.persistence.entity.DiscoveryRebuildRunEntity;
import com.grab.discovery.application.model.write.RebuildProgressResult;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DiscoveryRebuildRunJpaRepository extends JpaRepository<DiscoveryRebuildRunEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM DiscoveryRebuildRunEntity r WHERE r.stage <> :completed ORDER BY r.startedAt ASC")
    Optional<DiscoveryRebuildRunEntity> lockUnfinished(@Param("completed") RebuildProgressResult.Stage completed);
}
