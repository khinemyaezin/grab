package com.grab.discovery.adapter.persistence.adapter;

import com.grab.discovery.adapter.persistence.entity.DiscoveryRebuildRunEntity;
import com.grab.discovery.adapter.persistence.mapper.DiscoveryRebuildRunMapper;
import com.grab.discovery.adapter.persistence.repository.jpa.DiscoveryRebuildRunJpaRepository;
import com.grab.discovery.application.model.write.DiscoveryRebuildRun;
import com.grab.discovery.application.model.write.RebuildProgressResult;
import com.grab.discovery.application.port.outbound.DiscoveryRebuildRunRepository;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
public class DiscoveryRebuildRunRepositoryAdapter implements DiscoveryRebuildRunRepository {
    private static final String RESOURCE = "DiscoveryRebuildRunEntity";

    private final DiscoveryRebuildRunJpaRepository runs;
    private final DiscoveryRebuildRunMapper mapper;
    private final PersistenceExecutor executor;

    @Override
    public Optional<DiscoveryRebuildRun> lockUnfinished() {
        Optional<DiscoveryRebuildRunEntity> entity = executor.query(
                RESOURCE,
                () -> runs.lockUnfinished(RebuildProgressResult.Stage.COMPLETED)
        );
        return entity.map(mapper::toModel);
    }

    @Override
    public DiscoveryRebuildRun create(Id runId, Instant startedAt) {
        return executor.command(RESOURCE, () -> {
            DiscoveryRebuildRunEntity entity = new DiscoveryRebuildRunEntity();
            entity.setRunId(runId.getValue());
            entity.setStartedAt(startedAt);
            entity.setStage(RebuildProgressResult.Stage.PUBLICATIONS);
            entity.setProcessedCount(0);
            entity.setInvalidatedCount(0);
            return mapper.toModel(runs.saveAndFlush(entity));
        });
    }

    @Override
    public DiscoveryRebuildRun save(DiscoveryRebuildRun run) {
        return executor.command(RESOURCE, () -> {
            DiscoveryRebuildRunEntity entity = runs.findById(run.runId().getValue())
                    .orElseThrow(() -> new IllegalStateException("Rebuild run is missing."));
            if (entity.getVersion() != run.version()) {
                throw new ObjectOptimisticLockingFailureException(DiscoveryRebuildRunEntity.class, run.runId().getValue());
            }
            mapper.updateEntity(run, entity);
            return mapper.toModel(runs.saveAndFlush(entity));
        });
    }
}
