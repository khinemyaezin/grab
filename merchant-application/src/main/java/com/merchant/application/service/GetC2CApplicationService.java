package com.merchant.application.service;

import com.merchant.application.exception.MerchantServiceError;
import com.merchant.application.exception.MerchantServiceException;
import com.merchant.application.port.inbound.GetC2CApplicationUseCase;
import com.merchant.application.port.outbound.MerchantAccountQueryPort;
import com.merchant.application.model.read.GetC2CApplicationQuery;
import com.merchant.application.model.read.GetC2CApplicationResult;
import com.merchant.application.model.read.MerchantAccountView;
import com.merchant.domain.enums.MerchantType;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
public class GetC2CApplicationService implements GetC2CApplicationUseCase {

    private final MerchantAccountQueryPort merchantAccountQueryPort;

    public GetC2CApplicationResult execute(GetC2CApplicationQuery query) {
        return merchantAccountQueryPort.findByApplicantUserIdAndType(
                        query.applicantUserId().getValue(),
                        MerchantType.C2C_SELLER
                )
                .map(this::getC2CApplicationResult)
                .orElseThrow(() -> new MerchantServiceException(
                        new MerchantServiceError.MerchantNotFound("C2C profile for " + query.applicantUserId()),
                        "C2C Merchant profile not found"
                ));
    }

    private GetC2CApplicationResult getC2CApplicationResult(MerchantAccountView merchantAccount) {
        return new GetC2CApplicationResult(
                merchantAccount.merchantId(),
                merchantAccount.applicantUserId(),
                merchantAccount.type().name(),
                merchantAccount.status().name(),
                Objects.nonNull(merchantAccount.contactEmail()) || Objects.nonNull(merchantAccount.contactPhone()),
                merchantAccount.legalName() != null || merchantAccount.displayName() != null
        );
    }
}
