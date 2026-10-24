package com.grab.discovery.adapter.persistence.mapper;

import com.grab.discovery.adapter.persistence.entity.DiscoveryRebuildRunEntity;
import com.grab.discovery.application.model.write.DiscoveryRebuildRun;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class, unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class DiscoveryRebuildRunMapper {
    public abstract DiscoveryRebuildRun toModel(DiscoveryRebuildRunEntity entity);

    @Mapping(target = "version", ignore = true)
    public abstract void updateEntity(DiscoveryRebuildRun run, @MappingTarget DiscoveryRebuildRunEntity entity);
}
