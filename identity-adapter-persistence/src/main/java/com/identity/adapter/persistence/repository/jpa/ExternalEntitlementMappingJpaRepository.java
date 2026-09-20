package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.ExternalEntitlementMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ExternalEntitlementMappingJpaRepository extends JpaRepository<ExternalEntitlementMappingEntity, Long> {
    List<ExternalEntitlementMappingEntity> findByIssuerAndEntitlementIn(String issuer, Collection<String> entitlements);
}
