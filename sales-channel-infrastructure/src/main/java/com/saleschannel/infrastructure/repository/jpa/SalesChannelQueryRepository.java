package com.saleschannel.infrastructure.repository.jpa;

import com.saleschannel.infrastructure.specification.jpa.SalesChannelQueryCriteria;
import com.saleschannel.infrastructure.view.SalesChannelView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SalesChannelQueryRepository {
    Page<SalesChannelView> list(SalesChannelQueryCriteria criteria, Pageable pageable);

    Optional<SalesChannelView> findById(String id);
}
