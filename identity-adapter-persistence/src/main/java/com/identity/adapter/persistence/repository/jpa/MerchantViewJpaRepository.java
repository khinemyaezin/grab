package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.MerchantViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MerchantViewJpaRepository extends JpaRepository<MerchantViewEntity, String> {
    List<MerchantViewEntity> findAllByScopeIdIn(Collection<String> scopeIds);
}
