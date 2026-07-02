package com.grab.store.merchant.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.merchant.internal.config.MerchantReadTransactional;
import com.grab.store.merchant.internal.exception.MerchantServiceError;
import com.grab.store.merchant.internal.exception.MerchantServiceException;
import com.grab.store.merchant.internal.query.GetFirstPartyRetailerApplicationQuery;
import com.grab.store.merchant.internal.query.GetFirstPartyRetailerApplicationResult;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.enums.MerchantType;
import com.merchant.domain.repository.MerchantAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@MerchantEnabled
@RequiredArgsConstructor
public class GetFirstPartyRetailerApplicationQueryHandler
        implements QueryHandler<GetFirstPartyRetailerApplicationQuery, GetFirstPartyRetailerApplicationResult> {

    private final MerchantAccountRepository merchantAccountRepository;

    @Override
    @MerchantReadTransactional
    public GetFirstPartyRetailerApplicationResult handle(GetFirstPartyRetailerApplicationQuery query) {
        return merchantAccountRepository.findByApplicantUserId(query.applicantUserId())
                .stream()
                .filter(merchant -> merchant.getType() == MerchantType.FIRST_PARTY_RETAILER)
                .map(this::toResult)
                .findFirst()
                .orElseThrow(() -> new MerchantServiceException(
                        new MerchantServiceError.MerchantNotFound(
                                "First-party retailer profile for " + query.applicantUserId()),
                        "First-party retailer merchant profile not found"
                ));
    }

    @Override
    public Class<GetFirstPartyRetailerApplicationQuery> getQueryType() {
        return GetFirstPartyRetailerApplicationQuery.class;
    }

    private GetFirstPartyRetailerApplicationResult toResult(MerchantAccount merchant) {
        return new GetFirstPartyRetailerApplicationResult(
                merchant.getId().getValue(),
                merchant.getApplicantUserId().getValue(),
                merchant.getType().name(),
                merchant.getStatus().name(),
                Objects.nonNull(merchant.getContact()),
                Objects.nonNull(merchant.getName()),
                Objects.nonNull(merchant.getRegistration())
        );
    }
}
