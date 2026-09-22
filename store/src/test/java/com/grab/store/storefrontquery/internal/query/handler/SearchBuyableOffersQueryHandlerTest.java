package com.grab.store.storefrontquery.internal.query.handler;

import com.grab.store.saleschannel.port.SalesChannelQuery;
import com.grab.store.storefrontquery.internal.exception.StorefrontQueryServiceException;
import com.grab.store.storefrontquery.internal.query.SearchBuyableOffersQuery;
import com.storefrontquery.infrastructure.repository.jpa.BuyableOfferJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchBuyableOffersQueryHandlerTest {

    @Mock
    private BuyableOfferJpaRepository offers;
    @Mock
    private SalesChannelQuery salesChannels;
    @InjectMocks
    private SearchBuyableOffersQueryHandler handler;

    @Test
    void handle_channelDisabled_throwsStorefrontQueryServiceException() {
        when(salesChannels.isEnabled("web-1")).thenReturn(false);

        assertThatThrownBy(() -> handler.handle(new SearchBuyableOffersQuery("web-1", Pageable.unpaged())))
                .isInstanceOf(StorefrontQueryServiceException.class);
    }

    @Test
    void handle_channelEnabled_queriesBuyableOffers() {
        when(salesChannels.isEnabled("web-1")).thenReturn(true);
        when(offers.findBySalesChannelIdAndBuyableTrue(eq("web-1"), any()))
                .thenReturn(new PageImpl<>(List.of()));

        handler.handle(new SearchBuyableOffersQuery("web-1", Pageable.unpaged()));

        verify(offers).findBySalesChannelIdAndBuyableTrue(eq("web-1"), any());
    }
}
