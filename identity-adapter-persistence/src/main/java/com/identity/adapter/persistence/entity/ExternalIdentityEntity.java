package com.identity.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "external_identities", uniqueConstraints = @UniqueConstraint(columnNames = {"issuer", "subject"}))
public class ExternalIdentityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(nullable = false)
    private String issuer;

    @Column(nullable = false)
    private String subject;

    @Column(name = "linked_at", nullable = false)
    private LocalDateTime linkedAt;

    @Column(name = "directory_id")
    private String directoryId;

    @Column(name = "resource_id")
    private String resourceId;

    @Column(name = "linked_by")
    private String linkedBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "directory_revision", nullable = false)
    private long directoryRevision;

    @Column(name = "directory_observed_at")
    private LocalDateTime directoryObservedAt;
}
