package com.saleschannel.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "sales_channel_publishable_keys", indexes = {
        @Index(name = "idx_publishable_key_channel", columnList = "sales_channel_id")
})
public class PublishableKeyJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String uuid;

    @Column(name = "sales_channel_id", nullable = false, updatable = false)
    private String salesChannelId;

    @Column(nullable = false, length = 100, updatable = false)
    private String label;

    @Column(name = "token_digest", nullable = false, unique = true, length = 64, updatable = false)
    private String tokenDigest;

    @Column(name = "display_prefix", nullable = false, length = 32, updatable = false)
    private String displayPrefix;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Version
    @Column(nullable = false)
    private long version;
}
