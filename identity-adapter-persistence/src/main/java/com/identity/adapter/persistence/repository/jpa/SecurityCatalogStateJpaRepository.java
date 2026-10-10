package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.SecurityCatalogStateEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface SecurityCatalogStateJpaRepository extends JpaRepository<SecurityCatalogStateEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select state from SecurityCatalogStateEntity state where state.id = 1")
    SecurityCatalogStateEntity lockSingleton();
}
