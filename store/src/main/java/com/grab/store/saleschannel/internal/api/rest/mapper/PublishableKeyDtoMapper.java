package com.grab.store.saleschannel.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.saleschannel.internal.api.rest.dto.request.CreatePublishableKeyRequest;
import com.grab.store.saleschannel.internal.api.rest.dto.response.CreatedPublishableKeyResponse;
import com.grab.store.saleschannel.internal.api.rest.dto.response.PublishableKeyResponse;
import com.saleschannel.application.model.read.PublishableKeyView;
import com.saleschannel.application.model.write.CreatePublishableKeyCommand;
import com.saleschannel.application.model.write.CreatePublishableKeyResult;
import com.saleschannel.domain.valueobject.PublishableKeyManagementAccess;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class PublishableKeyDtoMapper {
    public abstract CreatePublishableKeyCommand toCreateCommand(
            String salesChannelId,
            CreatePublishableKeyRequest request,
            PublishableKeyManagementAccess access
    );

    public abstract PublishableKeyResponse toResponse(PublishableKeyView view);

    public CreatedPublishableKeyResponse toCreatedResponse(CreatePublishableKeyResult result) {
        return new CreatedPublishableKeyResponse(
                result.keyId().getValue(),
                result.salesChannelId().getValue(),
                result.label(),
                result.rawToken(),
                result.displayPrefix(),
                toString(result.createdAt())
        );
    }

    public com.saleschannel.application.model.write.RevokePublishableKeyCommand toRevokeCommand(
            String salesChannelId,
            String keyId,
            PublishableKeyManagementAccess access
    ) {
        return new com.saleschannel.application.model.write.RevokePublishableKeyCommand(
                new com.grab.framework.id.impl.CommonId(salesChannelId),
                new com.grab.framework.id.impl.CommonId(keyId),
                access
        );
    }

    public Pageable defaultPageable(Pageable pageable) {
        return pageable;
    }

    protected String toString(Instant value) {
        return value == null ? null : value.toString();
    }
}
