package com.cart.application.service;

import com.cart.application.exception.CartServiceError;
import com.cart.application.exception.CartServiceException;
import com.cart.application.model.read.GetCurrentCartQuery;
import com.cart.application.port.outbound.CartQueryPort;
import com.cart.application.model.read.CartLineView;
import com.cart.application.model.read.CartView;
import com.cart.domain.enums.CartStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCurrentCartServiceTest {

    @Mock
    private CartQueryPort cartQueryPort;

    @InjectMocks
    private GetCurrentCartService service;

    @Test
    void execute_requiresGuestToken() {
        assertThatThrownBy(() -> service.execute(new GetCurrentCartQuery("", "web-1", "default")))
                .isInstanceOf(CartServiceException.class)
                .extracting(ex -> ((CartServiceException) ex).getMessageSource())
                .isInstanceOf(CartServiceError.GuestTokenRequired.class);
    }

    @Test
    void execute_requiresSalesChannel() {
        assertThatThrownBy(() -> service.execute(new GetCurrentCartQuery("guest-a", "", "default")))
                .isInstanceOf(CartServiceException.class)
                .extracting(ex -> ((CartServiceException) ex).getMessageSource())
                .isInstanceOf(CartServiceError.SalesChannelRequired.class);
    }

    @Test
    void execute_returnsCartWhenFound() {
        when(cartQueryPort.findOpenByGuestToken("guest-a", "web-1", "default", CartStatus.OPEN))
                .thenReturn(Optional.of(new CartView(
                        "cart-1",
                        "guest-a",
                        "web-1",
                        "WEBSITE",
                        "default",
                        "MMK",
                        CartStatus.OPEN,
                        List.of(new CartLineView(
                                "line-1",
                                "var-1",
                                "prod-1",
                                "seller-1",
                                "Item",
                                "SKU-1",
                                new BigDecimal("10000"),
                                1
                        ))
                )));

        var result = service.execute(new GetCurrentCartQuery("guest-a", "web-1", "default"));

        assertThat(result.cartId()).isEqualTo("cart-1");
        assertThat(result.lines()).hasSize(1);
        assertThat(result.priceAdjusted()).isFalse();
    }

    @Test
    void execute_throwsWhenCartMissing() {
        when(cartQueryPort.findOpenByGuestToken("guest-a", "web-1", "default", CartStatus.OPEN))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new GetCurrentCartQuery("guest-a", "web-1", "default")))
                .isInstanceOf(CartServiceException.class)
                .extracting(ex -> ((CartServiceException) ex).getMessageSource())
                .isInstanceOf(CartServiceError.CartNotFound.class);
    }
}
