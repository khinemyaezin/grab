package com.grab.store.storefrontquery.internal.adapter.mapper;

import com.grab.discovery.application.port.outbound.PricingPort;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.id.impl.UuidGenerator;
import com.grab.framework.mapper.IdMapper;
import com.grab.store.pricing.port.PricingQuoteQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PricingQuoteMapperTest {
    private PricingQuoteMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PricingQuoteMapperImpl();
        UuidGenerator idGenerator = new UuidGenerator();
        IdMapper idMapper = new IdMapper(idGenerator);
        ReflectionTestUtils.setField(mapper, "idMapper", idMapper);
    }

    @Test
    void toQuotedPrice_pricedVariant_preservesZeroOriginalAmountCurrencyAndId() {
        BigDecimal originalAmount = new BigDecimal("8.50");
        PricingQuoteQuery.QuotedPrice source = new PricingQuoteQuery.QuotedPrice(
                BigDecimal.ZERO, originalAmount, "usd", "price-set-1"
        );

        PricingPort.QuotedPrice result = mapper.toQuotedPrice(source);

        CommonId expectedId = new CommonId("price-set-1");
        assertThat(result.priceSetId()).isEqualTo(expectedId);
        assertThat(result.amount()).isZero();
        assertThat(result.originalAmount()).isEqualByComparingTo(originalAmount);
        assertThat(result.currencyCode()).isEqualTo("usd");
    }

    @Test
    void toQuotedPrice_unpricedVariants_preservesUnpricedAndUnlinkedOutcomes() {
        PricingQuoteQuery.QuotedPrice source = new PricingQuoteQuery.QuotedPrice(null, null, "usd", null);

        PricingPort.QuotedPrice result = mapper.toQuotedPrice(source);

        assertThat(result.amount()).isNull();
        assertThat(result.originalAmount()).isNull();
        assertThat(result.priceSetId()).isNull();
        assertThat(result.currencyCode()).isEqualTo("usd");
    }
}
