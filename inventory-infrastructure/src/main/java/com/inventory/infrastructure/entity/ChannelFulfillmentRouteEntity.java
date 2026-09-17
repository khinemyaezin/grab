package com.inventory.infrastructure.entity;

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
@Table(name = "location_sales_channels")
@IdClass(ChannelFulfillmentRouteEntityId.class)
public class ChannelFulfillmentRouteEntity {

    @Id
    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Id
    @Column(name = "sales_channel_id", nullable = false)
    private String salesChannelId;
}
