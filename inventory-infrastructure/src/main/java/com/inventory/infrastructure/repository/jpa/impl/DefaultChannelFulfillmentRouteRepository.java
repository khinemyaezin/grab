package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import com.inventory.domain.aggregate.ChannelFulfillmentRoute;
import com.inventory.domain.repository.ChannelFulfillmentRouteRepository;
import com.inventory.infrastructure.entity.ChannelFulfillmentRouteEntity;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.mapper.jpa.ChannelFulfillmentRouteJpaAssembler;
import com.inventory.infrastructure.repository.jpa.ChannelFulfillmentRouteJpaRepository;
import com.inventory.infrastructure.repository.jpa.LocationJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultChannelFulfillmentRouteRepository implements ChannelFulfillmentRouteRepository {

    private final ChannelFulfillmentRouteJpaRepository jpaRepository;
    private final LocationJpaRepository locationJpaRepository;
    private final ChannelFulfillmentRouteJpaAssembler mapper;
    private final DomainEventProducer domainEventProducer;
    private final PersistenceExecutor executor;

    @Override
    public Optional<ChannelFulfillmentRoute> find(Id locationId, Id salesChannelId) {
        return executor.query("ChannelFulfillmentRoute", () -> {
            Optional<LocationEntity> location = locationJpaRepository.findByUuid(locationId.getValue());
            if (location.isEmpty()) {
                return Optional.empty();
            }
            LocationEntity locationEntity = location.get();
            Optional<ChannelFulfillmentRouteEntity> entity = jpaRepository.findByLocationIdAndSalesChannelId(
                    locationEntity.getId(),
                    salesChannelId.getValue());
            return entity.map(routeEntity -> mapper.toFullDomainGraph(routeEntity, locationEntity));
        });
    }

    @Override
    public List<ChannelFulfillmentRoute> findByLocationId(Id locationId) {
        return executor.query("ChannelFulfillmentRoute", () -> {
            Optional<LocationEntity> location = locationJpaRepository.findByUuid(locationId.getValue());
            if (location.isEmpty()) {
                return List.of();
            }
            LocationEntity locationEntity = location.get();
            List<ChannelFulfillmentRouteEntity> entities = jpaRepository.findByLocationId(locationEntity.getId());
            return entities.stream()
                    .map(entity -> mapper.toFullDomainGraph(entity, locationEntity))
                    .toList();
        });
    }

    @Override
    public boolean exists(Id locationId, Id salesChannelId) {
        return executor.query("ChannelFulfillmentRoute", () -> {
            Optional<LocationEntity> location = locationJpaRepository.findByUuid(locationId.getValue());
            if (location.isEmpty()) {
                return false;
            }
            LocationEntity locationEntity = location.get();
            return jpaRepository.existsByLocationIdAndSalesChannelId(
                    locationEntity.getId(),
                    salesChannelId.getValue());
        });
    }

    @Override
    public boolean existsActiveForMerchantAndChannel(Id merchantId, Id salesChannelId) {
        return executor.query("ChannelFulfillmentRoute", () ->
                jpaRepository.existsActiveForMerchantAndChannel(merchantId.getValue(), salesChannelId.getValue()));
    }

    @Override
    public void save(ChannelFulfillmentRoute route) {
        executor.command("ChannelFulfillmentRoute", () -> {
            LocationEntity location = locationJpaRepository.findByUuid(route.getLocationId().getValue())
                    .orElseThrow(() -> new IllegalStateException("Location not found for fulfillment route"));
            Optional<ChannelFulfillmentRouteEntity> existingEntity = jpaRepository.findByLocationIdAndSalesChannelId(
                    location.getId(),
                    route.getSalesChannelId().getValue());
            ChannelFulfillmentRouteEntity entity = mapper.buildFullEntityGraph(
                    route,
                    existingEntity.orElse(null),
                    location);
            jpaRepository.save(entity);
            List<Event> events = route.pullEvents();
            domainEventProducer.produce(
                    route.getClass().getSimpleName(),
                    route.getId().getValue(),
                    events);
            return null;
        });
    }

    @Override
    public void delete(ChannelFulfillmentRoute route) {
        executor.command("ChannelFulfillmentRoute", () -> {
            Optional<LocationEntity> location = locationJpaRepository.findByUuid(route.getLocationId().getValue());
            location.ifPresent(locationEntity -> {
                Optional<ChannelFulfillmentRouteEntity> entity = jpaRepository.findByLocationIdAndSalesChannelId(
                        locationEntity.getId(),
                        route.getSalesChannelId().getValue());
                entity.ifPresent(jpaRepository::delete);
            });
            List<Event> events = route.pullEvents();
            domainEventProducer.produce(
                    route.getClass().getSimpleName(),
                    route.getId().getValue(),
                    events);
            return null;
        });
    }
}
