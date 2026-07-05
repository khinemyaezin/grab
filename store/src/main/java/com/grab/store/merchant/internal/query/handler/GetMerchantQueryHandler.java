package com.grab.store.merchant.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.Id;
import com.grab.store.merchant.internal.command.MerchantAccountResult;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.merchant.internal.config.MerchantReadTransactional;
import com.grab.store.merchant.internal.exception.MerchantServiceError;
import com.grab.store.merchant.internal.exception.MerchantServiceException;
import com.grab.store.merchant.internal.query.GetMerchantQuery;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.repository.MerchantAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@MerchantEnabled
@RequiredArgsConstructor
public class GetMerchantQueryHandler implements QueryHandler<GetMerchantQuery, MerchantAccountResult> {
    private final MerchantAccountRepository merchants;

    @Override
    @MerchantReadTransactional
    public MerchantAccountResult handle(GetMerchantQuery query) {
        Id merchantId = query.merchantId();
        MerchantAccount merchant = merchants.findById(merchantId).orElseThrow(() -> notFound(merchantId));
        if (!query.reviewerAccess() && !query.scopedAccess()) {
            merchant.requireApplicant(query.actorId());
        }
        return MerchantAccountResult.from(merchant);
    }

    private MerchantServiceException notFound(Id merchantId) {
        MerchantServiceError error = new MerchantServiceError.MerchantNotFound(merchantId.getValue());
        return new MerchantServiceException(error, "Merchant account not found");
    }

    @Override
    public Class<GetMerchantQuery> getQueryType() {
        return GetMerchantQuery.class;
    }
}
