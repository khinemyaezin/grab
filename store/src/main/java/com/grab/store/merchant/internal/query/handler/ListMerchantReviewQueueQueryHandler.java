package com.grab.store.merchant.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.merchant.internal.config.MerchantReadTransactional;
import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.port.inbound.ListMerchantReviewQueueUseCase;
import com.merchant.application.model.read.ListMerchantReviewQueueQuery;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListMerchantReviewQueueQueryHandler implements QueryHandler<ListMerchantReviewQueueQuery, List<MerchantAccountResult>> {

    private final ListMerchantReviewQueueUseCase listMerchantReviewQueueUseCase;

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
