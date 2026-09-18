package com.merchant.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "storefront_channel_brand")
public class StorefrontChannelBrandEntity {
    @Id
    @Column(name = "storefront_id", nullable = false)
    private Long storefrontId;

    @Column(name = "sales_channel_id", nullable = false)
    private String salesChannelId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storefront_id", insertable = false, updatable = false)
    private StorefrontEntity storefront;
}
