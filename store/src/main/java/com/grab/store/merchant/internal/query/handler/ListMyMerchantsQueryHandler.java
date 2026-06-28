package com.grab.store.merchant.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.merchant.internal.command.MerchantAccountResult;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.merchant.internal.config.MerchantReadTransactional;
import com.grab.store.merchant.internal.query.ListMyMerchantsQuery;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.repository.MerchantAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@MerchantEnabled
@RequiredArgsConstructor
public class ListMyMerchantsQueryHandler implements QueryHandler<ListMyMerchantsQuery, List<MerchantAccountResult>> {
    private final MerchantAccountRepository merchants;

    @Override
    @MerchantReadTransactional
    public List<MerchantAccountResult> handle(ListMyMerchantsQuery query) {
        List<MerchantAccount> accounts = merchants.findByApplicantUserId(query.applicantUserId());
        return accounts.stream().map(MerchantAccountResult::from).toList();
    }

    @Override
    public Class<ListMyMerchantsQuery> getQueryType() {
        return ListMyMerchantsQuery.class;
    }
}
