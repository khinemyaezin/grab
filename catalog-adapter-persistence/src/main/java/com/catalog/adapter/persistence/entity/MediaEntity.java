package com.catalog.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "media")
public class MediaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "uuid", unique = true)
    private String uuid;

    @Column(name = "type")
    private String type;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "path", nullable = false)
    private String path;

    @Column(name = "storage_key", nullable = false, unique = true)
    private String storageKey;

    @Column(name = "url")
    private String url;

    @Column(name = "rank", nullable = false)
    private int rank;

    @ManyToMany(mappedBy = "medias")
    private Set<ProductEntity> products = new HashSet<>();

    @ManyToMany(mappedBy = "medias")
    private Set<ProductVariantEntity> productVariants = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MediaEntity that)) return false;
        return Objects.equals(uuid, that.uuid) && Objects.equals(storageKey, that.storageKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, storageKey);
    }
}
