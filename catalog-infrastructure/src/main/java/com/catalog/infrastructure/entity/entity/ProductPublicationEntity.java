package com.catalog.infrastructure.entity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "product_sales_channel_publication")
@IdClass(ProductPublicationEntityId.class)
public class ProductPublicationEntity {

    @Id
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Id
    @Column(name = "sales_channel_id", nullable = false)
    private String salesChannelId;
}
