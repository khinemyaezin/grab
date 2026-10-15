package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.ScopeDisplayEntity;
import com.identity.application.model.read.ScopeDisplayView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScopeDisplayJpaRepository extends JpaRepository<ScopeDisplayEntity, Long> {
    Optional<ScopeDisplayEntity> findByScopeId(String scopeId);

    @Query("""
            select new com.identity.application.model.read.ScopeDisplayView(
                display.scopeId, display.title, display.subtitle)
            from ScopeDisplayEntity display
            where display.scopeId in :scopeIds
            """)
    List<ScopeDisplayView> findDisplays(@Param("scopeIds") List<String> scopeIds);
}
