package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.ListSellerMerchantReviewQueueMapper;
import com.grab.app.platform.seller.internal.query.model.SellerMerchantQueries.ListSellerMerchantReviewQueueQuery;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.framework.cqrs.query.QueryBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SellerMerchantAdminQueryService {

    private final QueryBus queryBus;
    private final ListSellerMerchantReviewQueueMapper reviewQueueMapper;

    public SellerMerchantAdminQueryService(
            @Qualifier("sellerQueryBus") QueryBus queryBus,
            ListSellerMerchantReviewQueueMapper reviewQueueMapper
    ) {
        this.queryBus = queryBus;
        this.reviewQueueMapper = reviewQueueMapper;
    }

    public List<MerchantResponse> reviewQueue(String status) {
        List<MerchantOutboundResult> results = queryBus.dispatch(new ListSellerMerchantReviewQueueQuery(status));
        return reviewQueueMapper.toResponseList(results);
    }
}

