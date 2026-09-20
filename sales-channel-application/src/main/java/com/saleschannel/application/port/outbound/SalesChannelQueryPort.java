package com.saleschannel.application.port.outbound;

import com.saleschannel.application.model.read.SalesChannelQueryCriteria;
import com.saleschannel.application.model.read.SalesChannelView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SalesChannelQueryPort {
    Page<SalesChannelView> list(SalesChannelQueryCriteria criteria, Pageable pageable);

    Optional<SalesChannelView> findById(String id);
}
