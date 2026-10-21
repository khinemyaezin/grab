package com.grab.store.storefrontquery.internal.api.rest.service;

import com.grab.discovery.application.model.read.DiscoveryProductView;
import com.grab.discovery.application.model.read.GetBuyableOfferBySlugQuery;
import com.grab.discovery.application.model.read.SearchBuyableOffersQuery;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.storefrontquery.internal.api.rest.dto.response.BuyableOfferResponse;
import com.grab.store.storefrontquery.internal.api.rest.mapper.GetBuyableOfferBySlugRequestMapper;
import com.grab.store.storefrontquery.internal.api.rest.mapper.SearchBuyableOffersRequestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorefrontProductQueryServiceTest {

    @Mock
    private QueryBus queryBus;
    @Mock
    private SearchBuyableOffersRequestMapper searchMapper;
    @Mock
    private GetBuyableOfferBySlugRequestMapper getMapper;

    private StorefrontProductQueryService service;

    @BeforeEach
    void setUp() {
        service = new StorefrontProductQueryService(queryBus, searchMapper, getMapper);
    }

    @Test
    void list_validChannelAndPageable_returnsPagedResponses() {
        String salesChannelId = "web-1";
        Pageable pageable = Pageable.unpaged();
        SearchBuyableOffersQuery query = new SearchBuyableOffersQuery(new CommonId(salesChannelId), pageable);
        DiscoveryProductView view = createView();
        Page<DiscoveryProductView> viewPage = new PageImpl<>(List.of(view));
        BuyableOfferResponse response = createResponse();

        when(searchMapper.toQuery(salesChannelId, pageable)).thenReturn(query);
        when(queryBus.dispatch(query)).thenReturn(viewPage);
        when(searchMapper.toResponse(view)).thenReturn(response);

        Page<BuyableOfferResponse> actual = service.list(salesChannelId, pageable);

        assertThat(actual.getContent()).containsExactly(response);
        verify(queryBus).dispatch(query);
    }

    @Test
    void getBySlug_validChannelAndSlug_returnsResponse() {
        String salesChannelId = "web-1";
        String slug = "iced-coffee";
        GetBuyableOfferBySlugQuery query = new GetBuyableOfferBySlugQuery(new CommonId(salesChannelId), slug);
        DiscoveryProductView view = createView();
        BuyableOfferResponse response = createResponse();

        when(getMapper.toQuery(salesChannelId, slug)).thenReturn(query);
        when(queryBus.dispatch(query)).thenReturn(view);
        when(getMapper.toResponse(view)).thenReturn(response);

        BuyableOfferResponse actual = service.getBySlug(salesChannelId, slug);

        assertThat(actual).isEqualTo(response);
        verify(queryBus).dispatch(query);
    }

    private DiscoveryProductView createView() {
        return new DiscoveryProductView(
                new CommonId("web-1"),
                new CommonId("prod-1"),
                new CommonId("seller-1"),
                "Iced Coffee",
                "iced-coffee",
                null,
                null,
                "ACTIVE",
                true,
                true,
                "MMK",
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(5000),
                List.of(),
                List.of()
        );
    }

    private BuyableOfferResponse createResponse() {
        return new BuyableOfferResponse(
                "web-1",
                "prod-1",
                "seller-1",
                "Iced Coffee",
                "iced-coffee",
                null,
                null,
                "ACTIVE",
                true,
                true,
                "MMK",
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(5000),
                List.of(),
                List.of()
        );
    }
}
