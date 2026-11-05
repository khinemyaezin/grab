package com.grab.app.account.merchant.internal.query.handler;

import com.grab.app.account.merchant.internal.config.MerchantReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.merchant.application.model.read.ListMerchantReviewQueueQuery;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.port.inbound.ListMerchantReviewQueueUseCase;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListMerchantReviewQueueQueryHandler
        implements QueryHandler<ListMerchantReviewQueueQuery, List<MerchantAccountResult>> {

    private final ListMerchantReviewQueueUseCase listMerchantReviewQueueUseCase;

    public ListMerchantReviewQueueQueryHandler(ListMerchantReviewQueueUseCase listMerchantReviewQueueUseCase) {
        this.listMerchantReviewQueueUseCase = listMerchantReviewQueueUseCase;
    }

    @Override
    @MerchantReadTransactional
    public List<MerchantAccountResult> handle(ListMerchantReviewQueueQuery query) {
        return listMerchantReviewQueueUseCase.execute(query);
    }

    @Override
    public Class<ListMerchantReviewQueueQuery> getQueryType() {
        return ListMerchantReviewQueueQuery.class;
    }
}
