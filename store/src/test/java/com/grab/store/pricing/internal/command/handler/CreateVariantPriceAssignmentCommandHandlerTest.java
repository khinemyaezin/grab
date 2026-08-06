package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.pricing.internal.command.CreateVariantPriceAssignmentCommand;
import com.grab.store.pricing.internal.command.CreateVariantPriceAssignmentResult;
import com.pricing.domain.aggregate.PriceSet;
import com.pricing.domain.repository.PriceSetRepository;
import com.pricing.infrastructure.repository.jpa.VariantPriceSetLinkRepository;
import com.pricing.infrastructure.view.VariantPriceSetLinkView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateVariantPriceAssignmentCommandHandlerTest {

    @Mock
    private PriceSetRepository priceSetRepository;
    @Mock
    private VariantPriceSetLinkRepository variantPriceSetLinkRepository;

    private IdGenerator idGenerator;
    private CreateVariantPriceAssignmentCommandHandler handler;

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
        handler = new CreateVariantPriceAssignmentCommandHandler(
                priceSetRepository,
                variantPriceSetLinkRepository,
                idGenerator
        );
    }

    @Test
    void handle_shouldCreatePriceSetSaveLinkAndReturnPriceSetId() {
        when(priceSetRepository.save(any(PriceSet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateVariantPriceAssignmentResult result = handler.handle(new CreateVariantPriceAssignmentCommand(
                "variant-1",
                "product-1",
                "SKU-1",
                "merchant-1",
                "Base",
                "USD",
                new BigDecimal("19.99"),
                null,
                null,
                List.of()
        ));

        assertThat(result.priceSetId()).isEqualTo("id-1");

        ArgumentCaptor<PriceSet> priceSetCaptor = ArgumentCaptor.forClass(PriceSet.class);
        verify(priceSetRepository).save(priceSetCaptor.capture());
        PriceSet savedPriceSet = priceSetCaptor.getValue();
        assertThat(savedPriceSet.getId().getValue()).isEqualTo("id-1");
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
}
