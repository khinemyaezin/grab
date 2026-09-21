package com.cart.adapter.persistence.mapper.jpa;

import com.cart.domain.aggregate.Cart;
import com.cart.domain.entity.CartLine;
import com.cart.adapter.persistence.entity.CartEntity;
import com.cart.adapter.persistence.entity.CartLineEntity;
import com.grab.framework.mapper.IdMapper;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CartJpaAssembler {
    private final IdMapper ids;

    public CartEntity toEntity(Cart source, CartEntity destination) {
        CartEntity entity = destination == null ? new CartEntity() : destination;
        if (entity.getUuid() == null) {
            entity.setUuid(source.getId().getValue());
        }
        entity.setSalesChannelId(source.getSalesChannelId().getValue());
        entity.setChannelType(source.getChannelType());
        entity.setRegionId(source.getRegionId() == null ? null : source.getRegionId().getValue());
        entity.setCurrencyCode(source.getCurrencyCode());
        entity.setGuestToken(source.getGuestToken());
        entity.setStatus(source.getStatus());
        entity.setCreatedAt(source.getCreatedAt());
        entity.setUpdatedAt(source.getUpdatedAt());

        Map<String, CartLineEntity> existingByUuid = entity.getLines().stream()
                .filter(line -> line.getUuid() != null)
                .collect(Collectors.toMap(CartLineEntity::getUuid, line -> line, (left, right) -> left, LinkedHashMap::new));
        Set<String> keep = source.getLines().stream().map(line -> line.getId().getValue()).collect(Collectors.toSet());
        entity.getLines().removeIf(line -> line.getUuid() == null || !keep.contains(line.getUuid()));
        for (CartLine line : source.getLines()) {
            CartLineEntity lineEntity = existingByUuid.get(line.getId().getValue());
            if (lineEntity == null) {
                lineEntity = new CartLineEntity();
                lineEntity.setUuid(line.getId().getValue());
                entity.addLine(lineEntity);
            }
            lineEntity.setVariantId(line.getVariantId().getValue());
            lineEntity.setProductId(line.getProductId() == null ? null : line.getProductId().getValue());
            lineEntity.setSellerId(line.getSellerId().getValue());
            lineEntity.setTitle(line.getTitle());
            lineEntity.setSku(line.getSku());
            lineEntity.setUnitPrice(line.getUnitPrice());
            lineEntity.setQuantity(line.getQuantity());
        }
        return entity;
    }

    public Cart toDomain(CartEntity source) {
        List<CartLine> lines = source.getLines().stream().map(this::toLine).toList();
        return new Cart(
                ids.map(source.getUuid()),
                ids.map(source.getSalesChannelId()),
                source.getChannelType(),
                ids.map(source.getRegionId()),
                source.getCurrencyCode(),
                source.getGuestToken(),
                source.getStatus(),
                lines,
                source.getCreatedAt(),
                source.getUpdatedAt()
        );
    }

    private CartLine toLine(CartLineEntity source) {
        return new CartLine(
                ids.map(source.getUuid()),
                ids.map(source.getVariantId()),
                ids.map(source.getProductId()),
                ids.map(source.getSellerId()),
                source.getTitle(),
                source.getSku(),
                source.getUnitPrice(),
                source.getQuantity()
        );
    }
}
