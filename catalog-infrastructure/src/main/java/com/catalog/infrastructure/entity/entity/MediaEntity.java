package com.catalog.infrastructure.entity.entity;

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
    private String type;

    @Column(unique = true, nullable = false)
    private String path;

    @ManyToMany(mappedBy = "medias")
    private Set<ProductEntity> products = new HashSet<>();

    @ManyToMany(mappedBy = "medias")
    private Set<ProductVariantEntity> productVariants = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MediaEntity that)) return false;
        return Objects.equals(uuid, that.uuid) && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, path);
    }
}
