package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.query.model.SellerMerchantQueries.ListSellerMerchantReviewQueueQuery;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListSellerMerchantReviewQueueQueryHandler
        implements QueryHandler<ListSellerMerchantReviewQueueQuery, List<MerchantOutboundResult>> {

    private final SellerMerchantOutboundPort outboundPort;

    public ListSellerMerchantReviewQueueQueryHandler(SellerMerchantOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public List<MerchantOutboundResult> handle(ListSellerMerchantReviewQueueQuery query) {
        return outboundPort.listReviewQueue(query.status());
    }

    @Override
    public Class<ListSellerMerchantReviewQueueQuery> getQueryType() {
        return ListSellerMerchantReviewQueueQuery.class;
    }
}

