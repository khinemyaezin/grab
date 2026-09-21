package com.catalog.application.service;

import com.catalog.application.model.read.FindPublishedVariantQuery;
import com.catalog.application.port.outbound.BuyabilityQueryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindPublishedVariantServiceTest {

    @Mock
    private BuyabilityQueryPort buyabilityQueryPort;

    @InjectMocks
    private FindPublishedVariantService service;

    @Test
    void execute_returnsEmptyWhenPortReturnsEmpty() {
        when(buyabilityQueryPort.findPublished("var-1", "web-1")).thenReturn(Optional.empty());

        assertThat(service.execute(new FindPublishedVariantQuery("var-1", "web-1"))).isEmpty();
    }

    @Test
    void execute_mapsSliceWhenPublished() {
        when(buyabilityQueryPort.findPublished("var-1", "web-1")).thenReturn(Optional.of(
                new BuyabilityQueryPort.VariantSlice(
                        "var-1", "prod-1", "seller-1", "SKU-1", "Item", "item", "ACTIVE", false, null
                )
        ));

        assertThat(service.execute(new FindPublishedVariantQuery("var-1", "web-1")))
                .hasValueSatisfying(result -> {
                    assertThat(result.variantId()).isEqualTo("var-1");
                    assertThat(result.productStatus()).isEqualTo("ACTIVE");
                    assertThat(result.active()).isTrue();
                });
    }
}
