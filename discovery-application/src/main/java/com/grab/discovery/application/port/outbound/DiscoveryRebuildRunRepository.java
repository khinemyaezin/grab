package com.grab.discovery.application.port.outbound;

import com.grab.discovery.application.model.write.DiscoveryRebuildRun;
import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.Optional;

public interface DiscoveryRebuildRunRepository {
    Optional<DiscoveryRebuildRun> lockUnfinished();

    DiscoveryRebuildRun create(Id runId, Instant startedAt);

    DiscoveryRebuildRun save(DiscoveryRebuildRun run);
}
