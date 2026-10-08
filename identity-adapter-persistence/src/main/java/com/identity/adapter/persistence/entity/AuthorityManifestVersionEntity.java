package com.identity.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "security_authority_manifest_versions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_security_authority_manifest_module", columnNames = "module_key")
})
public class AuthorityManifestVersionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "module_key", nullable = false, updatable = false)
    private String moduleKey;

    @Column(name = "manifest_version", nullable = false)
    private int manifestVersion;

    @Column(name = "content_digest", nullable = false)
    private String contentDigest;

    @Version
    @Column(name = "row_version", nullable = false)
    private Long rowVersion;
}
