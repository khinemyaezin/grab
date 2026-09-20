package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.pricing.application.model.write.CreateVariantPriceAssignmentCommand;
import com.pricing.application.model.write.CreateVariantPriceAssignmentResult;
import com.pricing.application.service.CreateVariantPriceAssignmentService;
import com.pricing.domain.aggregate.PriceSet;
import com.pricing.domain.port.outbound.PriceSetRepository;
import com.pricing.domain.port.outbound.VariantPriceSetLinkRepository;
import com.pricing.domain.valueobject.VariantPriceSetLink;
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
class CreateVariantPriceAssignmentServiceTest {

    @Mock
    private PriceSetRepository priceSetRepository;
    @Mock
    private VariantPriceSetLinkRepository variantPriceSetLinkRepository;

    private IdGenerator idGenerator;
    private CreateVariantPriceAssignmentService service;

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
        service = new CreateVariantPriceAssignmentService(
                priceSetRepository,
                variantPriceSetLinkRepository,
                idGenerator
        );
    }

    @Test
    void execute_shouldCreatePriceSetSaveLinkAndReturnPriceSetId() {
        when(priceSetRepository.save(any(PriceSet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateVariantPriceAssignmentResult result = service.execute(new CreateVariantPriceAssignmentCommand(
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

        ArgumentCaptor<VariantPriceSetLink> linkCaptor = ArgumentCaptor.forClass(VariantPriceSetLink.class);
        verify(variantPriceSetLinkRepository).save(linkCaptor.capture());
        VariantPriceSetLink savedLink = linkCaptor.getValue();
        assertThat(savedLink.variantId()).isEqualTo("variant-1");
        assertThat(savedLink.priceSetId()).isEqualTo("id-1");
        assertThat(savedLink.productId()).isEqualTo("product-1");
        assertThat(savedLink.sku()).isEqualTo("SKU-1");
        assertThat(savedLink.merchantId()).isEqualTo("merchant-1");
    }
}
