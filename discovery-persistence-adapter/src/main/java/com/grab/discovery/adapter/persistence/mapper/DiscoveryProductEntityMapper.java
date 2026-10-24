package com.grab.discovery.adapter.persistence.mapper;

import com.grab.discovery.adapter.persistence.entity.DiscoveryProductEntity;
import com.grab.discovery.application.model.projection.DiscoveryProductProjection;
import com.grab.discovery.application.model.read.DiscoveryProductView;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(
        config = CentralMapperConfig.class,
        uses = IdMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        collectionMappingStrategy = CollectionMappingStrategy.TARGET_IMMUTABLE
)
public abstract class DiscoveryProductEntityMapper {

    public abstract DiscoveryProductView toView(DiscoveryProductEntity entity);

    public abstract DiscoveryProductView.ProductOption toViewOption(DiscoveryProductProjection.ProductOption option);

    public abstract List<DiscoveryProductView.ProductOption> toViewOptions(List<DiscoveryProductProjection.ProductOption> options);

    public abstract DiscoveryProductView.VariantFact toViewFact(DiscoveryProductProjection.VariantFact fact);

    public abstract List<DiscoveryProductView.VariantFact> toViewFacts(List<DiscoveryProductProjection.VariantFact> facts);

    public abstract DiscoveryProductProjection toProjection(DiscoveryProductEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    public abstract void updateEntity(DiscoveryProductProjection projection, @MappingTarget DiscoveryProductEntity entity);
}
