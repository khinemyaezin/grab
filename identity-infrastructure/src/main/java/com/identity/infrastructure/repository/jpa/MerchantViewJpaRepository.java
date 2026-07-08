package com.identity.infrastructure.repository.jpa;

import com.identity.infrastructure.entity.MerchantViewEntity;
import com.identity.infrastructure.view.MerchantView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MerchantViewJpaRepository extends JpaRepository<MerchantViewEntity, String> {
    List<MerchantView> findAllByScopeIdIn(Collection<String> scopeIds);
}
