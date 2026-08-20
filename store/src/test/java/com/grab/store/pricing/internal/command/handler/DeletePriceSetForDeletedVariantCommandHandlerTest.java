package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.pricing.internal.command.DeletePriceSetForDeletedVariantCommand;
import com.grab.store.pricing.internal.command.DeletePriceSetForDeletedVariantResult;
import com.pricing.domain.aggregate.PriceSet;
import com.pricing.domain.repository.PriceSetRepository;
import com.pricing.infrastructure.repository.jpa.VariantPriceSetLinkQueryRepository;
import com.pricing.infrastructure.repository.jpa.VariantPriceSetLinkRepository;
import com.pricing.infrastructure.view.VariantPriceSetLinkView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeletePriceSetForDeletedVariantCommandHandlerTest {

    @Mock
    private VariantPriceSetLinkQueryRepository variantPriceSetLinkQueryRepository;
    @Mock
    private VariantPriceSetLinkRepository variantPriceSetLinkRepository;
    @Mock
    private PriceSetRepository priceSetRepository;

    private DeletePriceSetForDeletedVariantCommandHandler handler;

    @BeforeEach
    void setUp() {
        IdGenerator idGenerator = new IdGenerator() {
            @Override
            public Id generateId() {
                return new CommonId("new");
            }

            @Override
            public Id convertIdFrom(String id) {
                return new CommonId(id);
            }
        };
        handler = new DeletePriceSetForDeletedVariantCommandHandler(
                variantPriceSetLinkQueryRepository,
                variantPriceSetLinkRepository,
                priceSetRepository,
                idGenerator
        );
    }

    @Test
    void handle_shouldDeleteLinkAndPriceSet() {
        CommonId variantId = new CommonId("variant-1");
        CommonId priceSetId = new CommonId("price-set-1");
        when(variantPriceSetLinkQueryRepository.findByVariantIds(List.of("variant-1"))).thenReturn(List.of(
                new VariantPriceSetLinkView(
                        "variant-1",
                        "price-set-1",
                        "product-1",
                        "SKU-1",
                        "merchant-1",
                        Instant.now(),
                        Instant.now()
                )
        ));
        when(priceSetRepository.findById(priceSetId)).thenReturn(Optional.of(PriceSet.create(priceSetId, Instant.now())));

        DeletePriceSetForDeletedVariantResult result = handler.handle(
                new DeletePriceSetForDeletedVariantCommand(variantId)
        );

        assertThat(result.deleted()).isTrue();
        assertThat(result.priceSetId()).isEqualTo("price-set-1");
        verify(variantPriceSetLinkRepository).deleteByVariantId("variant-1");
        verify(priceSetRepository).delete(priceSetId);
    }

    @Test
    void handle_whenNoLink_shouldSkip() {
        CommonId variantId = new CommonId("variant-1");
        when(variantPriceSetLinkQueryRepository.findByVariantIds(List.of("variant-1"))).thenReturn(List.of());

        DeletePriceSetForDeletedVariantResult result = handler.handle(
                new DeletePriceSetForDeletedVariantCommand(variantId)
        );

        assertThat(result.deleted()).isFalse();
        verifyNoInteractions(priceSetRepository);
        verifyNoInteractions(variantPriceSetLinkRepository);
    }
}
