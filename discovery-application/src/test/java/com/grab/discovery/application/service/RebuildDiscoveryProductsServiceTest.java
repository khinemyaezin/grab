package com.grab.discovery.application.service;

import com.grab.discovery.application.model.projection.DiscoveryProductProjection;
import com.grab.discovery.application.model.read.DiscoveryProductKey;
import com.grab.discovery.application.model.write.DiscoveryRebuildRun;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshProductChannel;
import com.grab.discovery.application.model.write.RebuildDiscoveryProductsCommand;
import com.grab.discovery.application.model.write.RebuildProgressResult;
import com.grab.discovery.application.model.write.RebuildProgressResult.Stage;
import com.grab.discovery.application.port.inbound.ProjectDiscoveryProductUseCase;
import com.grab.discovery.application.port.outbound.CatalogPort;
import com.grab.discovery.application.port.outbound.DiscoveryProductRepository;
import com.grab.discovery.application.port.outbound.DiscoveryRebuildRunRepository;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RebuildDiscoveryProductsServiceTest {
    private static final Id RUN_ID = new CommonId("run-1");
    private static final Id PRODUCT_ID = new CommonId("product-1");
    private static final Id CHANNEL_ID = new CommonId("channel-1");
    private static final Instant STARTED_AT = Instant.parse("2026-10-01T00:00:00Z");

    @Mock
    private CatalogPort catalog;
    @Mock
    private DiscoveryProductRepository projections;
    @Mock
    private DiscoveryRebuildRunRepository runs;
    @Mock
    private ProjectDiscoveryProductUseCase projector;
    @Mock
    private IdGenerator idGenerator;
    @Captor
    private ArgumentCaptor<DiscoveryRebuildRun> runCaptor;
    @Captor
    private ArgumentCaptor<ProjectDiscoveryProductCommand> commandCaptor;
    @InjectMocks
    private RebuildDiscoveryProductsService service;

    @Test
    void execute_newRun_processesBoundedBatchWithStartWatermark() {
        when(idGenerator.generateId()).thenReturn(RUN_ID);
        when(runs.create(eq(RUN_ID), any(Instant.class))).thenAnswer(invocation -> {
            Instant startedAt = invocation.getArgument(1);
            return new DiscoveryRebuildRun(RUN_ID, startedAt, Stage.PUBLICATIONS, null, null, 0, 0, 0);
        });
        CatalogPort.ProductChannelPair pair = new CatalogPort.ProductChannelPair(PRODUCT_ID, CHANNEL_ID);
        when(catalog.findPublishedProductChannelsAfter(null, null, 1)).thenReturn(List.of(pair));
        when(runs.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        RebuildDiscoveryProductsCommand command = new RebuildDiscoveryProductsCommand(1);

        RebuildProgressResult result = service.execute(command);

        assertThat(result.stage()).isEqualTo(Stage.PUBLICATIONS);
        assertThat(result.processedCount()).isEqualTo(1);
        assertThat(result.lastProductId()).isEqualTo(PRODUCT_ID);
        assertThat(result.lastSalesChannelId()).isEqualTo(CHANNEL_ID);
        assertThat(result.complete()).isFalse();
        verify(runs).save(runCaptor.capture());
        verify(projector).execute(commandCaptor.capture());
        RefreshProductChannel update = (RefreshProductChannel) commandCaptor.getValue().update();
        assertThat(update.occurredAt()).isEqualTo(runCaptor.getValue().startedAt());
        assertThat(update.salesChannelId()).isEqualTo(CHANNEL_ID);
        assertThat(update.productId()).isEqualTo(PRODUCT_ID);
    }

    @Test
    void execute_restartedRun_resumesCursorAndCompletesBothPasses() {
        DiscoveryRebuildRun publicationRun = run(Stage.PUBLICATIONS, PRODUCT_ID, CHANNEL_ID, 1);
        DiscoveryRebuildRun projectionRun = run(Stage.EXISTING_PROJECTIONS, null, null, 1);
        when(runs.lockUnfinished()).thenReturn(Optional.of(publicationRun), Optional.of(projectionRun));
        when(runs.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        RebuildDiscoveryProductsCommand command = new RebuildDiscoveryProductsCommand(1);

        RebuildProgressResult publicationPass = service.execute(command);
        RebuildDiscoveryProductsService restartedProcess = new RebuildDiscoveryProductsService(
                catalog, projections, runs, projector, idGenerator
        );
        RebuildProgressResult projectionPass = restartedProcess.execute(command);

        assertThat(publicationPass.stage()).isEqualTo(Stage.EXISTING_PROJECTIONS);
        assertThat(publicationPass.lastProductId()).isNull();
        assertThat(publicationPass.lastSalesChannelId()).isNull();
        assertThat(projectionPass.stage()).isEqualTo(Stage.COMPLETED);
        assertThat(projectionPass.complete()).isTrue();
        assertThat(projectionPass.processedCount()).isEqualTo(1);
        verify(catalog).findPublishedProductChannelsAfter(PRODUCT_ID, CHANNEL_ID, 1);
        verify(projections).findKeysAfter(null, null, 1);
        verify(runs, times(2)).save(any());
        verifyNoInteractions(projector, idGenerator);
    }

    @Test
    void execute_existingProjectionStage_reconcilesAndCountsInvalidations() {
        DiscoveryRebuildRun run = run(Stage.EXISTING_PROJECTIONS, null, null, 10);
        DiscoveryProductKey key = new DiscoveryProductKey(PRODUCT_ID, CHANNEL_ID);
        DiscoveryProductProjection before = projection(true, STARTED_AT.minusSeconds(1));
        DiscoveryProductProjection after = projection(false, STARTED_AT);
        when(runs.lockUnfinished()).thenReturn(Optional.of(run));
        when(projections.findKeysAfter(null, null, 2)).thenReturn(List.of(key));
        when(projections.findBySalesChannelAndProduct(CHANNEL_ID, PRODUCT_ID))
                .thenReturn(Optional.of(before), Optional.of(after));
        when(runs.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        RebuildDiscoveryProductsCommand command = new RebuildDiscoveryProductsCommand(2);

        RebuildProgressResult result = service.execute(command);

        assertThat(result.complete()).isTrue();
        assertThat(result.processedCount()).isEqualTo(11);
        assertThat(result.invalidatedCount()).isEqualTo(1);
        verify(projector).execute(commandCaptor.capture());
        RefreshProductChannel update = (RefreshProductChannel) commandCaptor.getValue().update();
        assertThat(update.occurredAt()).isEqualTo(STARTED_AT);
        verifyNoInteractions(catalog, idGenerator);
        verify(projections, never()).save(any());
    }

    @Test
    void execute_newerLiveEvent_doesNotCountAsInvalidation() {
        DiscoveryRebuildRun run = run(Stage.EXISTING_PROJECTIONS, null, null, 0);
        DiscoveryProductKey key = new DiscoveryProductKey(PRODUCT_ID, CHANNEL_ID);
        DiscoveryProductProjection newer = projection(true, STARTED_AT.plusSeconds(10));
        when(runs.lockUnfinished()).thenReturn(Optional.of(run));
        when(projections.findKeysAfter(null, null, 2)).thenReturn(List.of(key));
        when(projections.findBySalesChannelAndProduct(CHANNEL_ID, PRODUCT_ID)).thenReturn(Optional.of(newer));
        when(runs.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        RebuildDiscoveryProductsCommand command = new RebuildDiscoveryProductsCommand(2);

        RebuildProgressResult result = service.execute(command);

        assertThat(result.invalidatedCount()).isZero();
        verify(projector).execute(commandCaptor.capture());
        RefreshProductChannel update = (RefreshProductChannel) commandCaptor.getValue().update();
        assertThat(update.occurredAt()).isBefore(newer.updatedAt());
        verify(projections, never()).save(any());
    }

    @Test
    void execute_reconciliationFailure_keepsCommittedCheckpoint() {
        DiscoveryRebuildRun run = run(Stage.PUBLICATIONS, null, null, 0);
        CatalogPort.ProductChannelPair pair = new CatalogPort.ProductChannelPair(PRODUCT_ID, CHANNEL_ID);
        IllegalStateException failure = new IllegalStateException("source unavailable");
        when(runs.lockUnfinished()).thenReturn(Optional.of(run));
        when(catalog.findPublishedProductChannelsAfter(null, null, 1)).thenReturn(List.of(pair));
        doThrow(failure).when(projector).execute(any());
        RebuildDiscoveryProductsCommand command = new RebuildDiscoveryProductsCommand(1);

        assertThatThrownBy(() -> service.execute(command)).isSameAs(failure);

        verify(runs, never()).save(any());
        verifyNoInteractions(idGenerator);
    }

    @Test
    void execute_invalidBatchSize_rejectsBeforePortAccess() {
        RebuildDiscoveryProductsCommand command = new RebuildDiscoveryProductsCommand(0);

        assertThatThrownBy(() -> service.execute(command)).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(catalog, projections, runs, projector, idGenerator);
    }

    private DiscoveryRebuildRun run(Stage stage, Id productId, Id channelId, long processed) {
        return new DiscoveryRebuildRun(RUN_ID, STARTED_AT, stage, productId, channelId, processed, 0, 3);
    }

    private DiscoveryProductProjection projection(boolean published, Instant watermark) {
        return new DiscoveryProductProjection(
                CHANNEL_ID, PRODUCT_ID, null, null, "Product", null, null, null,
                "ACTIVE", published, true, false, List.of(), List.of(), watermark, 2
        );
    }
}
