package com.saleschannel.domain.service;

import com.grab.framework.id.impl.CommonId;
import com.saleschannel.domain.exception.SalesChannelDomainException;
import com.saleschannel.domain.repository.SalesChannelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesChannelUniquenessServiceTest {
    @Mock
    private SalesChannelRepository salesChannels;

    @Test
    void requireWebsiteAvailable_whenExists_shouldReject() {
        when(salesChannels.existsWebsiteByMerchantId(new CommonId("merchant-1"))).thenReturn(true);
        SalesChannelUniquenessService service = new SalesChannelUniquenessService(salesChannels);

        assertThatThrownBy(() -> service.requireWebsiteAvailable(new CommonId("merchant-1")))
                .isInstanceOf(SalesChannelDomainException.class);
    }

    @Test
    void requireMarketplaceAvailable_whenExists_shouldReject() {
        when(salesChannels.existsMarketplace()).thenReturn(true);
        SalesChannelUniquenessService service = new SalesChannelUniquenessService(salesChannels);

        assertThatThrownBy(service::requireMarketplaceAvailable)
                .isInstanceOf(SalesChannelDomainException.class);
    }
}
