package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.pricing.internal.command.UpdateVariantPriceCommand;
import com.grab.store.pricing.internal.command.UpdateVariantPriceResult;
import com.pricing.domain.aggregate.PriceSet;
import com.pricing.domain.entity.Price;
import com.pricing.domain.repository.PriceSetRepository;
import com.pricing.domain.valueobject.CurrencyCode;
import com.pricing.domain.valueobject.MoneyAmount;
import com.pricing.infrastructure.repository.jpa.VariantPriceSetLinkRepository;
import com.pricing.infrastructure.view.VariantPriceSetLinkView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateVariantPriceCommandHandlerTest {

    @Mock
    private PriceSetRepository priceSetRepository;
    @Mock
    private VariantPriceSetLinkRepository variantPriceSetLinkRepository;

    private IdGenerator idGenerator;
    private UpdateVariantPriceCommandHandler handler;

    @BeforeEach
    void setUp() {
        idGenerator = new IdGenerator() {
            private int counter;

            @Override
            public com.grab.framework.id.Id generateId() {
                return new CommonId("id-" + (++counter));
            }

            @Override
            public com.grab.framework.id.Id convertIdFrom(String id) {
                return new CommonId(id);
            }
        };
        handler = new UpdateVariantPriceCommandHandler(
                priceSetRepository,
                variantPriceSetLinkRepository,
                idGenerator
        );
    }

    @Test
    void handle_whenLinkMissing_shouldCreatePriceSetAndLink() {
        when(variantPriceSetLinkRepository.findByVariantId("variant-1")).thenReturn(Optional.empty());
        when(priceSetRepository.save(any(PriceSet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateVariantPriceResult result = handler.handle(command("SKU-1", "USD", "19.99", null, null));

        assertThat(result.priceSetId()).isEqualTo("id-1");
        assertThat(result.priceId()).isEqualTo("id-2");
        assertThat(result.priceSetCreated()).isTrue();

        ArgumentCaptor<PriceSet> priceSetCaptor = ArgumentCaptor.forClass(PriceSet.class);
        verify(priceSetRepository).save(priceSetCaptor.capture());
        PriceSet savedPriceSet = priceSetCaptor.getValue();
        assertThat(savedPriceSet.getPrices()).hasSize(1);
        assertThat(savedPriceSet.getPrices().getFirst().getAmount().value()).isEqualByComparingTo("19.99");
        assertThat(savedPriceSet.getPrices().getFirst().getCurrencyCode().value()).isEqualTo("usd");

        ArgumentCaptor<VariantPriceSetLinkView> linkCaptor = ArgumentCaptor.forClass(VariantPriceSetLinkView.class);
        verify(variantPriceSetLinkRepository).save(linkCaptor.capture());
        VariantPriceSetLinkView savedLink = linkCaptor.getValue();
        assertThat(savedLink.variantId()).isEqualTo("variant-1");
        assertThat(savedLink.priceSetId()).isEqualTo("id-1");
        assertThat(savedLink.productId()).isEqualTo("product-1");
        assertThat(savedLink.sku()).isEqualTo("SKU-1");
        assertThat(savedLink.merchantId()).isEqualTo("merchant-1");
    }

    @Test
    void handle_whenLinkFound_shouldUpdatePriceSetAndSku() {
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        PriceSet priceSet = existingPriceSet("price-set-1", "price-1", "usd", "10.00", createdAt);
        when(variantPriceSetLinkRepository.findByVariantId("variant-1")).thenReturn(Optional.of(
                existingLink("OLD-SKU", createdAt)
        ));
        when(priceSetRepository.findById(any())).thenReturn(Optional.of(priceSet));
        when(priceSetRepository.save(any(PriceSet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateVariantPriceResult result = handler.handle(command("NEW-SKU", "USD", "12.00", null, null));

        assertThat(result.priceSetId()).isEqualTo("price-set-1");
        assertThat(result.priceId()).isEqualTo("price-1");
        assertThat(result.priceSetCreated()).isFalse();

        ArgumentCaptor<PriceSet> priceSetCaptor = ArgumentCaptor.forClass(PriceSet.class);
        verify(priceSetRepository).save(priceSetCaptor.capture());
        PriceSet savedPriceSet = priceSetCaptor.getValue();
        assertThat(savedPriceSet.getPrices()).hasSize(1);
        assertThat(savedPriceSet.getPrices().getFirst().getAmount().value()).isEqualByComparingTo("12.00");

        ArgumentCaptor<VariantPriceSetLinkView> linkCaptor = ArgumentCaptor.forClass(VariantPriceSetLinkView.class);
        verify(variantPriceSetLinkRepository).save(linkCaptor.capture());
        VariantPriceSetLinkView savedLink = linkCaptor.getValue();
        assertThat(savedLink.sku()).isEqualTo("NEW-SKU");
        assertThat(savedLink.priceSetId()).isEqualTo("price-set-1");
        assertThat(savedLink.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void handle_whenExistingCurrencyMatches_shouldReplaceAmount() {
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        PriceSet priceSet = existingPriceSet("price-set-1", "price-1", "usd", "10.00", createdAt);
        when(variantPriceSetLinkRepository.findByVariantId("variant-1")).thenReturn(Optional.of(
                existingLink("SKU-1", createdAt)
        ));
        when(priceSetRepository.findById(any())).thenReturn(Optional.of(priceSet));
        when(priceSetRepository.save(any(PriceSet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateVariantPriceResult result = handler.handle(command("SKU-1", "USD", "25.00", null, null));

        assertThat(result.priceSetCreated()).isFalse();
        assertThat(result.priceId()).isEqualTo("price-1");
        ArgumentCaptor<PriceSet> priceSetCaptor = ArgumentCaptor.forClass(PriceSet.class);
        verify(priceSetRepository).save(priceSetCaptor.capture());
        assertThat(priceSetCaptor.getValue().getPrices()).hasSize(1);
        assertThat(priceSetCaptor.getValue().getPrices().getFirst().getAmount().value())
                .isEqualByComparingTo("25.00");
    }

    @Test
    void handle_whenNewCurrency_shouldAddPrice() {
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        PriceSet priceSet = existingPriceSet("price-set-1", "price-1", "usd", "10.00", createdAt);
        when(variantPriceSetLinkRepository.findByVariantId("variant-1")).thenReturn(Optional.of(
                existingLink("SKU-1", createdAt)
        ));
        when(priceSetRepository.findById(any())).thenReturn(Optional.of(priceSet));
        when(priceSetRepository.save(any(PriceSet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateVariantPriceResult result = handler.handle(command("SKU-1", "MMK", "15000", null, null));

        assertThat(result.priceSetCreated()).isFalse();
        assertThat(result.priceId()).isEqualTo("id-1");
        ArgumentCaptor<PriceSet> priceSetCaptor = ArgumentCaptor.forClass(PriceSet.class);
        verify(priceSetRepository).save(priceSetCaptor.capture());
        PriceSet saved = priceSetCaptor.getValue();
        assertThat(saved.getPrices()).hasSize(2);
        assertThat(saved.findMatchingBasePrice(CurrencyCode.of("usd"), null, null)).isPresent();
        assertThat(saved.findMatchingBasePrice(CurrencyCode.of("mmk"), null, null)).isPresent();
        assertThat(saved.findMatchingBasePrice(CurrencyCode.of("mmk"), null, null).get().getAmount().value())
                .isEqualByComparingTo("15000");
    }

    @Test
    void handle_whenOrphanLink_shouldCreatePriceSetAndRewriteLink() {
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        when(variantPriceSetLinkRepository.findByVariantId("variant-1")).thenReturn(Optional.of(
                existingLink("SKU-1", createdAt)
        ));
        when(priceSetRepository.findById(any())).thenReturn(Optional.empty());
        when(priceSetRepository.save(any(PriceSet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateVariantPriceResult result = handler.handle(command("SKU-1", "USD", "19.99", null, null));

        assertThat(result.priceSetCreated()).isTrue();
        assertThat(result.priceSetId()).isEqualTo("id-1");
        assertThat(result.priceId()).isEqualTo("id-2");

        ArgumentCaptor<VariantPriceSetLinkView> linkCaptor = ArgumentCaptor.forClass(VariantPriceSetLinkView.class);
        verify(variantPriceSetLinkRepository).save(linkCaptor.capture());
        assertThat(linkCaptor.getValue().priceSetId()).isEqualTo("id-1");
        assertThat(linkCaptor.getValue().sku()).isEqualTo("SKU-1");
    }

    private UpdateVariantPriceCommand command(
            String sku,
            String currencyCode,
            String amount,
            Integer minQuantity,
            Integer maxQuantity
    ) {
        return new UpdateVariantPriceCommand(
                "variant-1",
                "product-1",
                sku,
                "merchant-1",
                "Base",
                currencyCode,
                new BigDecimal(amount),
                minQuantity,
                maxQuantity,
                List.of()
        );
    }

    private PriceSet existingPriceSet(
            String priceSetId,
            String priceId,
            String currency,
            String amount,
            Instant createdAt
    ) {
        PriceSet priceSet = PriceSet.create(new CommonId(priceSetId), createdAt);
        Price price = Price.createBase(
                new CommonId(priceId),
                priceSet.getId(),
                "Base",
                CurrencyCode.of(currency),
                MoneyAmount.of(new BigDecimal(amount)),
                null,
                null,
                List.of()
        );
        priceSet.addPrice(price, createdAt);
        return priceSet;
    }

    private VariantPriceSetLinkView existingLink(String sku, Instant createdAt) {
        return new VariantPriceSetLinkView(
                "variant-1",
                "price-set-1",
                "product-1",
                sku,
                "merchant-1",
                createdAt,
                createdAt
        );
    }
}
