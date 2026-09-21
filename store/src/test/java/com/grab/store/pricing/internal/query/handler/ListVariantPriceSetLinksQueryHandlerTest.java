package com.grab.store.pricing.internal.query.handler;

import com.pricing.application.port.outbound.VariantPriceSetLinkQueryPort;
import com.pricing.application.model.read.ListVariantPriceSetLinksQuery;
import com.pricing.application.model.read.VariantPriceSetLinkResult;
import com.pricing.application.model.read.VariantPriceSetLinkView;
import com.pricing.application.service.ListVariantPriceSetLinksService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListVariantPriceSetLinksServiceTest {

    @Mock
    private VariantPriceSetLinkQueryPort variantPriceSetLinkQueryPort;

    private ListVariantPriceSetLinksService service;

    @BeforeEach
    void setUp() {
        service = new ListVariantPriceSetLinksService(variantPriceSetLinkQueryPort);
    }

    @Test
    void execute_shouldReturnLinksForRequestedVariantIds() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        when(variantPriceSetLinkQueryPort.findByVariantIds(List.of("variant-1", "variant-2")))
                .thenReturn(List.of(
                        new VariantPriceSetLinkView(
                                "variant-1", "price-set-1", "product-1", "SKU-1", "merchant-1", now, now
                        ),
                        new VariantPriceSetLinkView(
                                "variant-2", "price-set-2", "product-1", "SKU-2", "merchant-1", now, now
                        )
                ));

        List<VariantPriceSetLinkResult> results = service.execute(
                new ListVariantPriceSetLinksQuery(List.of("variant-1", "variant-2"))
        );

        assertThat(results).containsExactly(
                new VariantPriceSetLinkResult("variant-1", "price-set-1", "product-1", "SKU-1", "merchant-1"),
                new VariantPriceSetLinkResult("variant-2", "price-set-2", "product-1", "SKU-2", "merchant-1")
        );
    }
}
