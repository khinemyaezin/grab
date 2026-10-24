package com.grab.discovery.adapter.persistence.entity;

import com.grab.discovery.application.model.write.RebuildProgressResult;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "discovery_rebuild_run")
public class DiscoveryRebuildRunEntity {
    @Id
    @Column(name = "run_id", nullable = false, length = 255)
    private String runId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RebuildProgressResult.Stage stage;

    @Column(name = "last_product_id")
    private String lastProductId;

    @Column(name = "last_sales_channel_id")
    private String lastSalesChannelId;

    @Column(name = "processed_count", nullable = false)
    private long processedCount;

    @Column(name = "invalidated_count", nullable = false)
    private long invalidatedCount;

    @Version
    @Column(nullable = false)
    private long version;
}
