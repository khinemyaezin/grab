package com.saleschannel.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "sales_channel_regions")
@Getter
@Setter
public class ChannelRegionEntity {
    @EmbeddedId
    private ChannelRegionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_channel_id", referencedColumnName = "uuid", nullable = false)
    private SalesChannelEntity salesChannel;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled;

    @Column(name = "is_default", nullable = false)
    private boolean defaultRegion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
