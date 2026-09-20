package com.merchant.application.service;

import com.merchant.application.exception.MerchantServiceError;
import com.merchant.application.exception.MerchantServiceException;
import com.merchant.application.port.inbound.GetFirstPartyRetailerApplicationUseCase;
import com.merchant.application.port.outbound.MerchantAccountQueryPort;
import com.merchant.application.model.read.GetFirstPartyRetailerApplicationQuery;
import com.merchant.application.model.read.GetFirstPartyRetailerApplicationResult;
import com.merchant.application.model.read.MerchantAccountView;
import com.merchant.domain.enums.MerchantType;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
public class GetFirstPartyRetailerApplicationService implements GetFirstPartyRetailerApplicationUseCase {

    private final MerchantAccountQueryPort merchantAccountQueryPort;

    public GetFirstPartyRetailerApplicationResult execute(GetFirstPartyRetailerApplicationQuery query) {
        return merchantAccountQueryPort.findByApplicantUserIdAndType(
                        query.applicantUserId().getValue(),
                        MerchantType.FIRST_PARTY_RETAILER
                )
                .map(this::toResult)
                .orElseThrow(() -> new MerchantServiceException(
                        new MerchantServiceError.MerchantNotFound(
                                "First-party retailer profile for " + query.applicantUserId()),
                        "First-party retailer merchant profile not found"
                ));
    }

    private GetFirstPartyRetailerApplicationResult toResult(MerchantAccountView merchant) {
        return new GetFirstPartyRetailerApplicationResult(
                merchant.merchantId(),
                merchant.applicantUserId(),
                merchant.type().name(),
                merchant.status().name(),
                Objects.nonNull(merchant.contactEmail()) || Objects.nonNull(merchant.contactPhone()),
                merchant.legalName() != null || merchant.displayName() != null,
                merchant.registrationCountryCode() != null || merchant.registrationNumber() != null
        );
    }
}
