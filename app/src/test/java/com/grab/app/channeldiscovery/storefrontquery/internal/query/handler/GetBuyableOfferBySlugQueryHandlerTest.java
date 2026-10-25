package com.grab.app.channeldiscovery.storefrontquery.internal.query.handler;

import com.grab.discovery.application.model.read.BuyableOfferResult;
import com.grab.discovery.application.model.read.GetBuyableOfferBySlugQuery;
import com.grab.discovery.application.model.read.ShoppingContext;
import com.grab.discovery.application.port.inbound.GetBuyableOfferBySlugUseCase;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
        GetBuyableOfferBySlugQuery query = new GetBuyableOfferBySlugQuery(
                new ShoppingContext(new CommonId("web-1"), new CommonId("region-1"), "SG", "SGD", "en-SG"),
                true, "iced-coffee");
        BuyableOfferResult product = new BuyableOfferResult(
                new CommonId("web-1"), new CommonId("region-1"), "SG", "SGD", "en-SG", true,
                new CommonId("product-1"), new CommonId("seller-1"), new CommonId("category-1"),
                "Iced Coffee", "iced-coffee", null, null, "ACTIVE", true, true, true, true,
                null, null, List.of(), List.of());
        when(getBuyableOfferBySlugUseCase.execute(query)).thenReturn(product);

        BuyableOfferResult result = handler.handle(query);

        assertThat(result).isEqualTo(product);
        verify(getBuyableOfferBySlugUseCase).execute(query);
    }
}
