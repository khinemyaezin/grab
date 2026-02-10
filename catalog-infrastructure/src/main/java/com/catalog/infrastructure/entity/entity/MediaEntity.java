package com.catalog.infrastructure.entity.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
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
}
