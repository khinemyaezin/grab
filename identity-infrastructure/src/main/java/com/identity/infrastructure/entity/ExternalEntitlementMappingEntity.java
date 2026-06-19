package com.identity.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "external_entitlement_mappings", uniqueConstraints = @UniqueConstraint(columnNames = {"issuer", "entitlement", "role_id"}))
public class ExternalEntitlementMappingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String issuer;

    @Column(nullable = false)
    private String entitlement;

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id")
    private RoleEntity role;
}
