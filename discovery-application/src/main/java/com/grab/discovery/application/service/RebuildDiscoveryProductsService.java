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
import com.grab.discovery.application.port.inbound.RebuildDiscoveryProductsUseCase;
import com.grab.discovery.application.port.outbound.CatalogPort;
import com.grab.discovery.application.port.outbound.DiscoveryProductRepository;
import com.grab.discovery.application.port.outbound.DiscoveryRebuildRunRepository;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class RebuildDiscoveryProductsService implements RebuildDiscoveryProductsUseCase {

    private final CatalogPort catalog;
    private final DiscoveryProductRepository projections;
    private final DiscoveryRebuildRunRepository runs;
    private final ProjectDiscoveryProductUseCase projectDiscoveryProductUseCase;
    private final IdGenerator idGenerator;

    @Override
    public RebuildProgressResult execute(RebuildDiscoveryProductsCommand command) {
        if (command.batchSize() < 1) {
            throw new IllegalArgumentException("Rebuild batch size must be positive.");
        }
        DiscoveryRebuildRun run = runs.lockUnfinished().orElseGet(this::startRun);
        if (run.stage() == Stage.PUBLICATIONS) {
            return processPublications(run, command.batchSize());
        }
        if (run.stage() == Stage.EXISTING_PROJECTIONS) {
            return processExistingProjections(run, command.batchSize());
        }
        return result(run, true);
    }

    private DiscoveryRebuildRun startRun() {
        Id runId = idGenerator.generateId();
        Instant startedAt = Instant.now();
        return runs.create(runId, startedAt);
    }

    private RebuildProgressResult processPublications(DiscoveryRebuildRun run, int batchSize) {
        List<CatalogPort.ProductChannelPair> publishedPairs = catalog.findPublishedProductChannelsAfter(
                run.lastProductId(),
                run.lastSalesChannelId(),
                batchSize
        );
        List<DiscoveryProductKey> batch = publishedPairs.stream()
                .map(pair -> new DiscoveryProductKey(pair.productId(), pair.salesChannelId()))
                .toList();
        Id lastProductId = run.lastProductId();
        Id lastChannelId = run.lastSalesChannelId();
        long processedCount = run.processedCount();
        long invalidatedCount = run.invalidatedCount();
        for (DiscoveryProductKey key : batch) {
            invalidatedCount += refreshAndCountInvalidation(key, run.startedAt());
            processedCount++;
            lastProductId = key.productId();
            lastChannelId = key.salesChannelId();
        }
        Stage nextStage = batch.size() < batchSize ? Stage.EXISTING_PROJECTIONS : Stage.PUBLICATIONS;
        if (nextStage == Stage.EXISTING_PROJECTIONS) {
            lastProductId = null;
            lastChannelId = null;
        }
        DiscoveryRebuildRun updated = new DiscoveryRebuildRun(
                run.runId(),
                run.startedAt(),
                nextStage,
                lastProductId,
                lastChannelId,
                processedCount,
                invalidatedCount,
                run.version()
        );
        DiscoveryRebuildRun saved = runs.save(updated);
        return result(saved, false);
    }

    private RebuildProgressResult processExistingProjections(DiscoveryRebuildRun run, int batchSize) {
        List<DiscoveryProductKey> batch = projections.findKeysAfter(
                run.lastProductId(),
                run.lastSalesChannelId(),
                batchSize
        );
        Id lastProductId = run.lastProductId();
        Id lastChannelId = run.lastSalesChannelId();
        long processedCount = run.processedCount();
        long invalidatedCount = run.invalidatedCount();
        for (DiscoveryProductKey key : batch) {
            invalidatedCount += refreshAndCountInvalidation(key, run.startedAt());
            processedCount++;
            lastProductId = key.productId();
            lastChannelId = key.salesChannelId();
        }
        Stage nextStage = batch.size() < batchSize ? Stage.COMPLETED : Stage.EXISTING_PROJECTIONS;
        DiscoveryRebuildRun updated = new DiscoveryRebuildRun(
                run.runId(),
                run.startedAt(),
                nextStage,
                lastProductId,
                lastChannelId,
                processedCount,
                invalidatedCount,
                run.version()
        );
        DiscoveryRebuildRun saved = runs.save(updated);
        return result(saved, saved.stage() == Stage.COMPLETED);
    }

    private long refreshAndCountInvalidation(DiscoveryProductKey key, Instant watermark) {
        Optional<DiscoveryProductProjection> before = projections.findBySalesChannelAndProduct(
                key.salesChannelId(),
                key.productId()
        );
        RefreshProductChannel update = new RefreshProductChannel(key.productId(), key.salesChannelId(), watermark);
        ProjectDiscoveryProductCommand projectCommand = new ProjectDiscoveryProductCommand(update);
        projectDiscoveryProductUseCase.execute(projectCommand);
        Optional<DiscoveryProductProjection> after = projections.findBySalesChannelAndProduct(
                key.salesChannelId(),
                key.productId()
        );
        boolean wasPublished = before.map(DiscoveryProductProjection::published).orElse(false);
        boolean isPublished = after.map(DiscoveryProductProjection::published).orElse(false);
        return wasPublished && !isPublished ? 1 : 0;
    }

    private RebuildProgressResult result(DiscoveryRebuildRun run, boolean complete) {
        return new RebuildProgressResult(
                run.runId(),
                run.stage(),
                run.lastProductId(),
                run.lastSalesChannelId(),
                run.processedCount(),
                run.invalidatedCount(),
                complete
        );
    }
}
