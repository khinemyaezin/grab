package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.id.impl.CommonId;
import com.grab.store.pricing.internal.command.DeletePriceSetCommand;
import com.grab.store.pricing.internal.exception.PricingServiceException;
import com.pricing.domain.aggregate.PriceSet;
import com.pricing.domain.repository.PriceSetRepository;
import com.pricing.infrastructure.repository.jpa.VariantPriceSetLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeletePriceSetCommandHandlerTest {

    @Mock
    private PriceSetRepository priceSetRepository;
    @Mock
    private VariantPriceSetLinkRepository variantPriceSetLinkRepository;

    private DeletePriceSetCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DeletePriceSetCommandHandler(priceSetRepository, variantPriceSetLinkRepository);
    }

    @Test
    void handle_shouldDeleteLinkThenPriceSet() {
        CommonId priceSetId = new CommonId("price-set-1");
        PriceSet priceSet = PriceSet.create(priceSetId, Instant.now());
        when(priceSetRepository.findById(priceSetId)).thenReturn(Optional.of(priceSet));

        handler.handle(new DeletePriceSetCommand(priceSetId));

        verify(variantPriceSetLinkRepository).deleteByPriceSetId("price-set-1");
        verify(priceSetRepository).delete(priceSetId);
    }

    @Test
    void handle_whenPriceSetNotFound_shouldNotDeleteLink() {
        CommonId priceSetId = new CommonId("missing");
        when(priceSetRepository.findById(priceSetId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new DeletePriceSetCommand(priceSetId)))
                .isInstanceOf(PricingServiceException.class);

        verifyNoInteractions(variantPriceSetLinkRepository);
    }
}
