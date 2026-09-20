package com.cart.adapter.persistence.entity;

import com.cart.domain.enums.CartStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "carts", indexes = {
        @Index(name = "idx_cart_guest_channel_region", columnList = "guest_token, sales_channel_id, region_id, status"),
        @Index(name = "idx_cart_uuid", columnList = "uuid", unique = true)
})
public class CartEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String uuid;

    @Column(name = "sales_channel_id", nullable = false)
    private String salesChannelId;

    @Column(name = "channel_type", nullable = false)
    private String channelType;

    @Column(name = "region_id")
    private String regionId;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(name = "guest_token", nullable = false)
    private String guestToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CartStatus status;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CartLineEntity> lines = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    public void addLine(CartLineEntity line) {
        line.setCart(this);
        lines.add(line);
    }
}
