package com.identity.infrastructure.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "role_delegation_rules",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_role_delegation_rule",
                columnNames = {"delegator_role_id", "delegated_role_id"}
        )
)
public class RoleDelegationRuleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "delegator_role_id", nullable = false)
    private RoleEntity delegatorRole;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "delegated_role_id", nullable = false)
    private RoleEntity delegatedRole;
}
