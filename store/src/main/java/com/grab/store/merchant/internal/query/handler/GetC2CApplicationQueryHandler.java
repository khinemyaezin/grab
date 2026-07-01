package com.grab.store.merchant.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.merchant.internal.config.MerchantEnabled;
import com.grab.store.merchant.internal.config.MerchantReadTransactional;
import com.grab.store.merchant.internal.exception.MerchantServiceError;
import com.grab.store.merchant.internal.exception.MerchantServiceException;
import com.grab.store.merchant.internal.query.GetC2CApplicationQuery;
import com.grab.store.merchant.internal.query.GetC2CApplicationResult;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.enums.MerchantType;
import com.merchant.domain.repository.MerchantAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@MerchantEnabled
@RequiredArgsConstructor
public class GetC2CApplicationQueryHandler implements QueryHandler<GetC2CApplicationQuery, GetC2CApplicationResult> {

    private final MerchantAccountRepository merchantAccountRepository;

    @Override
    @MerchantReadTransactional
    public GetC2CApplicationResult handle(GetC2CApplicationQuery query) {
        return merchantAccountRepository.findByApplicantUserId(query.applicantUserId())
                .stream()
                .filter(merchant -> merchant.getType() == MerchantType.C2C_SELLER)
                .map(this::getC2CApplicationResult)
                .findFirst()
                .orElseThrow(() -> new MerchantServiceException(
                        new MerchantServiceError.MerchantNotFound("C2C profile for " + query.applicantUserId()),
                        "C2C Merchant profile not found"
                ));
    }

    @Override
    public Class<GetC2CApplicationQuery> getQueryType() {
        return GetC2CApplicationQuery.class;
    }

    private GetC2CApplicationResult getC2CApplicationResult(MerchantAccount merchantAccount) {
        return new GetC2CApplicationResult(
                merchantAccount.getId().getValue(),
                merchantAccount.getApplicantUserId().getValue(),
                merchantAccount.getType().name(),
                merchantAccount.getStatus().name(),
                Objects.nonNull(merchantAccount.getContact()),
                Objects.nonNull(merchantAccount.getName())
        );
    }
}
