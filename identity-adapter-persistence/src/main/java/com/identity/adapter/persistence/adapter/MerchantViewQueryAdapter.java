package com.identity.adapter.persistence.adapter;

import com.identity.application.port.outbound.MerchantViewQueryPort;
import com.identity.application.model.read.MerchantScopeView;
import com.identity.application.model.read.MerchantView;
import com.identity.adapter.persistence.entity.MerchantViewEntity;
import com.identity.adapter.persistence.repository.jpa.MerchantViewJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class MerchantViewQueryAdapter implements MerchantViewQueryPort {

    private final MerchantViewJpaRepository merchantViewJpaRepository;

    @Override
    public List<MerchantView> findAllByScopeIdIn(Collection<String> scopeIds) {
        return merchantViewJpaRepository.findAllByScopeIdIn(scopeIds).stream()
                .map(MerchantViewQueryAdapter::toView)
                .toList();
    }

    private static MerchantView toView(MerchantViewEntity entity) {
        return new MerchantScopeView(entity.getScopeId(), entity.getName(), entity.getStatus());
    }
}
