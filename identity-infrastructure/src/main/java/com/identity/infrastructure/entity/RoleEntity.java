package com.identity.infrastructure.entity;

import com.identity.domain.enums.RoleKind;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "roles")
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String uuid;

    @Column(nullable = false, unique = true, updatable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_kind", nullable = false)
    private RoleKind kind = RoleKind.CUSTOM;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean assignable = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "role_authorities", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "authority_id"))
    private Set<AuthorityEntity> authorities = new LinkedHashSet<>();
}
