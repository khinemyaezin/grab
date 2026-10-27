package com.grab;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.cqrs.command.impl.DefaultCommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.id.impl.UuidGenerator;
import com.grab.app.account.identity.internal.command.handler.UpdateScopeDisplayCommandHandler;
import com.grab.app.account.identity.internal.adapter.inbound.event.MerchantScopeDisplayProjectionListener;
import com.grab.app.account.merchant.internal.adapter.outbound.event.MerchantIntegrationEventPublisher;
import com.grab.app.account.merchant.internal.adapter.outbound.event.MerchantProfileIntegrationEventPublisher;
import com.identity.application.model.write.UpdateScopeDisplayCommand;
import com.identity.application.port.outbound.ScopeDisplayViewRepository;
import com.identity.application.service.UpdateScopeDisplayService;
import com.merchant.domain.event.MerchantApplicationStartedEvent;
import com.merchant.domain.event.MerchantProfileUpdatedEvent;
import com.merchant.domain.event.StorefrontCreatedEvent;
import com.merchant.domain.event.StorefrontRenamedEvent;
import com.merchant.domain.port.outbound.MerchantAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class MerchantScopeDisplayEventFlowTest {
    private static final Instant OCCURRED_AT = Instant.parse("2026-10-11T10:00:00Z");

    private final RecordingScopeDisplays displays = new RecordingScopeDisplays();
    private final MerchantAccountRepository merchants = mock(MerchantAccountRepository.class);
    private AnnotationConfigApplicationContext context;

    @BeforeEach
    void setUp() {
        UpdateScopeDisplayService useCase = new UpdateScopeDisplayService(displays);
        UpdateScopeDisplayCommandHandler handler = new UpdateScopeDisplayCommandHandler(useCase);
        List<CommandHandler<?, ?>> handlers = List.of(handler);
        CommandBus commandBus = new DefaultCommandBus(handlers);
        IdGenerator ids = new UuidGenerator();
        TestPropertyValues properties = TestPropertyValues.of("merchant.enabled=true");
        context = new AnnotationConfigApplicationContext();
        properties.applyTo(context);
        context.registerBean(CommandBus.class, () -> commandBus);
        context.registerBean(IdGenerator.class, () -> ids);
        context.registerBean(MerchantAccountRepository.class, () -> merchants);
        context.register(MerchantProfileIntegrationEventPublisher.class,
                MerchantIntegrationEventPublisher.class, MerchantScopeDisplayProjectionListener.class);
        context.refresh();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void publishEvent_merchantStarted_createsMerchantDisplayThroughCommandBus() {
        MerchantApplicationStartedEvent event = new MerchantApplicationStartedEvent(
                "merchant-1", "Shop", "user-1", "DRAFT", "actor-1", 1L, OCCURRED_AT);
        Id merchantId = new CommonId("merchant-1");
        UpdateScopeDisplayCommand expected = new UpdateScopeDisplayCommand(merchantId, "Shop");

        context.publishEvent(event);

        assertThat(displays.updates).containsExactly(expected);
        assertThat(displays.titles).containsEntry("merchant-1", "Shop");
        verifyNoInteractions(merchants);
    }

    @Test
    void publishEvent_merchantProfileUpdated_updatesExistingDisplay() {
        displays.titles.put("merchant-1", "Old Shop");
        MerchantProfileUpdatedEvent event = new MerchantProfileUpdatedEvent(
                "merchant-1", "New Shop", 2L, OCCURRED_AT);
        Id merchantId = new CommonId("merchant-1");
        UpdateScopeDisplayCommand expected = new UpdateScopeDisplayCommand(merchantId, "New Shop");

        context.publishEvent(event);

        assertThat(displays.updates).containsExactly(expected);
        assertThat(displays.titles).hasSize(1).containsEntry("merchant-1", "New Shop");
        verifyNoInteractions(merchants);
    }

    @Test
    void publishEvent_storefrontCreated_updatesOnlyStorefrontDisplayOnce() {
        StorefrontCreatedEvent event = new StorefrontCreatedEvent(
                "storefront-1", "merchant-1", "Store", "store-slug", "DRAFT", 1L, OCCURRED_AT);
        Id storefrontId = new CommonId("storefront-1");
        UpdateScopeDisplayCommand expected = new UpdateScopeDisplayCommand(storefrontId, "Store");

        context.publishEvent(event);

        assertThat(displays.updates).containsExactly(expected);
        assertThat(displays.titles).hasSize(1).containsEntry("storefront-1", "Store");
        verifyNoInteractions(merchants);
    }

    @Test
    void publishEvent_storefrontRenamed_updatesStorefrontInsteadOfMerchant() {
        displays.titles.put("merchant-1", "Merchant");
        displays.titles.put("storefront-1", "Old Store");
        StorefrontRenamedEvent event = new StorefrontRenamedEvent(
                "storefront-1", "merchant-1", "Old Store", "New Store",
                "old-slug", "new-slug", 3L, OCCURRED_AT);
        Id storefrontId = new CommonId("storefront-1");
        UpdateScopeDisplayCommand expected = new UpdateScopeDisplayCommand(storefrontId, "New Store");

        context.publishEvent(event);

        assertThat(displays.updates).containsExactly(expected);
        assertThat(displays.titles).hasSize(2)
                .containsEntry("storefront-1", "New Store")
                .containsEntry("merchant-1", "Merchant");
        verifyNoInteractions(merchants);
    }

    @Test
    void publishEvent_repeatedDelivery_reappliesSameDisplaySnapshot() {
        MerchantProfileUpdatedEvent event = new MerchantProfileUpdatedEvent(
                "merchant-1", "New Shop", 2L, OCCURRED_AT);
        Id merchantId = new CommonId("merchant-1");
        UpdateScopeDisplayCommand expected = new UpdateScopeDisplayCommand(merchantId, "New Shop");

        context.publishEvent(event);
        context.publishEvent(event);

        assertThat(displays.updates).containsExactly(expected, expected);
        assertThat(displays.titles).hasSize(1).containsEntry("merchant-1", "New Shop");
    }

    @Test
    void publishEvent_projectionFailure_propagatesToPublisherAndAllowsRedelivery() {
        IllegalStateException failure = new IllegalStateException("Identity database unavailable");
        displays.failure = failure;
        MerchantProfileUpdatedEvent event = new MerchantProfileUpdatedEvent(
                "merchant-1", "New Shop", 2L, OCCURRED_AT);
        Id merchantId = new CommonId("merchant-1");
        UpdateScopeDisplayCommand expected = new UpdateScopeDisplayCommand(merchantId, "New Shop");

        assertThatThrownBy(() -> context.publishEvent(event)).isSameAs(failure);
        assertThat(displays.titles).isEmpty();

        displays.failure = null;
        context.publishEvent(event);

        assertThat(displays.updates).containsExactly(expected);
        assertThat(displays.titles).containsEntry("merchant-1", "New Shop");
    }

    private static class RecordingScopeDisplays implements ScopeDisplayViewRepository {
        private final Map<String, String> titles = new HashMap<>();
        private final List<UpdateScopeDisplayCommand> updates = new ArrayList<>();
        private RuntimeException failure;

        @Override
        public void update(Id scopeId, String name) {
            if (failure != null) {
                throw failure;
            }
            String scopeIdValue = scopeId.getValue();
            UpdateScopeDisplayCommand update = new UpdateScopeDisplayCommand(scopeId, name);
            titles.put(scopeIdValue, name);
            updates.add(update);
        }
    }
}
