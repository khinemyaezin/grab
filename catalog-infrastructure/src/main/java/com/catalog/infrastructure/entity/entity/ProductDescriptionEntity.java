package com.catalog.infrastructure.entity.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "product_description")
public class ProductDescriptionEntity extends DescriptionSuperEntity {

    @ManyToOne(targetEntity = ProductEntity.class)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;


}
