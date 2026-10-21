package com.grab.store.storefrontquery.internal.query.handler;

import com.grab.discovery.application.model.read.DiscoveryProductView;
import com.grab.discovery.application.model.read.SearchBuyableOffersQuery;
import com.grab.discovery.application.port.inbound.SearchBuyableOffersUseCase;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchBuyableOffersQueryHandlerTest {

    @Mock
    private SearchBuyableOffersUseCase searchBuyableOffersUseCase;
    @InjectMocks
    private SearchBuyableOffersQueryHandler handler;

    @Test
    void handle_validQuery_delegatesToUseCase() {
        SearchBuyableOffersQuery query = new SearchBuyableOffersQuery(new CommonId("web-1"), Pageable.unpaged());
        Page<DiscoveryProductView> expectedPage = new PageImpl<>(List.of());
        when(searchBuyableOffersUseCase.execute(query)).thenReturn(expectedPage);

        Page<DiscoveryProductView> result = handler.handle(query);

        assertThat(result).isSameAs(expectedPage);
        verify(searchBuyableOffersUseCase).execute(query);
    }
}

