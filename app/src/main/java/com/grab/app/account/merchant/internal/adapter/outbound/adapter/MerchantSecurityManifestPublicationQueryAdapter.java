package com.grab.app.account.merchant.internal.adapter.outbound;

import com.grab.app.account.merchant.internal.config.MerchantReadTransactional;
import com.grab.app.account.merchant.internal.config.MerchantEnabled;
import com.grab.app.account.merchant.internal.adapter.outbound.mapper.MerchantSecurityManifestPublicationQueryMapper;
import com.grab.app.account.merchant.port.MerchantSecurityManifestPublicationQuery;
import com.merchant.application.port.inbound.GetMerchantSecurityManifestPublicationStatusUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@MerchantEnabled
@RequiredArgsConstructor
public class MerchantSecurityManifestPublicationQueryAdapter implements MerchantSecurityManifestPublicationQuery {
    private final GetMerchantSecurityManifestPublicationStatusUseCase useCase;
    private final MerchantSecurityManifestPublicationQueryMapper mapper;

    @Override
    @MerchantReadTransactional
    public PublicationStatus status() {
        var view = useCase.execute();
        return mapper.toResponse(view);
    }
}
