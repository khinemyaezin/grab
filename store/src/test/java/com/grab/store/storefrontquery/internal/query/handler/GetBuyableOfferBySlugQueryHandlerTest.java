package com.grab.store.storefrontquery.internal.query.handler;

import com.grab.discovery.application.model.read.DiscoveryProductView;
import com.grab.discovery.application.model.read.GetBuyableOfferBySlugQuery;
import com.grab.discovery.application.port.inbound.GetBuyableOfferBySlugUseCase;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetBuyableOfferBySlugQueryHandlerTest {

    @Mock
    private GetBuyableOfferBySlugUseCase getBuyableOfferBySlugUseCase;
    @InjectMocks
    private GetBuyableOfferBySlugQueryHandler handler;

    @Test
    void handle_validQuery_delegatesToUseCase() {
        GetBuyableOfferBySlugQuery query = new GetBuyableOfferBySlugQuery(new CommonId("web-1"), "iced-coffee");
        DiscoveryProductView product = new DiscoveryProductView(
                new CommonId("web-1"),
                new CommonId("product-1"),
                new CommonId("seller-1"),
                "Iced Coffee",
                "iced-coffee",
                null,
                null,
                "ACTIVE",
                true,
                true,
                "MMK",
                BigDecimal.valueOf(5_000),
                BigDecimal.valueOf(5_000),
                List.of(),
                List.of(new DiscoveryProductView.VariantOffer(new CommonId("variant-1"), "SKU-1", "Iced Coffee",
                        Map.of(), BigDecimal.valueOf(5_000), null, 3, true, false, true, true))
        );
        when(getBuyableOfferBySlugUseCase.execute(query)).thenReturn(product);

        DiscoveryProductView result = handler.handle(query);

        assertThat(result).isEqualTo(product);
        verify(getBuyableOfferBySlugUseCase).execute(query);
    }
}

