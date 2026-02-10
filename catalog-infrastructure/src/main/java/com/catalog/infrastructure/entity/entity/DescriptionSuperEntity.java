package com.catalog.infrastructure.entity.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@MappedSuperclass
public class DescriptionSuperEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", unique = true)
    private String uuid;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 100)
    private String title;

    private String description;
}
