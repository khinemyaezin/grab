package com.saleschannel.application.service;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.saleschannel.application.exception.SalesChannelServiceException;
import com.saleschannel.application.model.read.ListPublishableKeysQuery;
import com.saleschannel.application.model.read.PublishableKeyView;
import com.saleschannel.application.model.read.ResolvePublishableKeyQuery;
import com.saleschannel.application.model.read.ResolvedPublishableKeyView;
import com.saleschannel.application.model.read.SalesChannelView;
import com.saleschannel.application.model.write.CreatePublishableKeyCommand;
import com.saleschannel.application.model.write.CreatePublishableKeyResult;
import com.saleschannel.application.model.write.RevokePublishableKeyCommand;
import com.saleschannel.application.port.outbound.PublishableKeyQueryPort;
import com.saleschannel.application.port.outbound.PublishableTokenGeneratorPort;
import com.saleschannel.application.port.outbound.PublishableTokenGeneratorPort.GeneratedToken;
import com.saleschannel.application.port.outbound.SalesChannelQueryPort;
import com.saleschannel.domain.aggregate.PublishableKey;
import com.saleschannel.domain.aggregate.SalesChannel;
import com.saleschannel.domain.enums.ChannelOwner;
import com.saleschannel.domain.enums.ChannelStatus;
import com.saleschannel.domain.enums.ChannelType;
import com.saleschannel.domain.exception.SalesChannelDomainException;
import com.saleschannel.domain.policy.PublishableKeyManagementPolicy;
import com.saleschannel.domain.port.outbound.PublishableKeyRepository;
import com.saleschannel.domain.port.outbound.SalesChannelRepository;
import com.saleschannel.domain.valueobject.PublishableKeyManagementAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PublishableKeyServicesTest {
    private final Id channelId = new CommonId("channel-1");
    private final Id merchantId = new CommonId("merchant-1");
    private final Id keyId = new CommonId("key-1");
    private final Instant now = Instant.parse("2026-10-23T00:00:00Z");
    private final String rawToken = "pk_" + "a".repeat(43);
    private final String digest = "b".repeat(64);
    private final PublishableKeyManagementAccess access = new PublishableKeyManagementAccess(merchantId, false);
    private final SalesChannelRepository channels = mock(SalesChannelRepository.class);
    private final SalesChannelQueryPort channelQueries = mock(SalesChannelQueryPort.class);
    private final PublishableKeyRepository keys = mock(PublishableKeyRepository.class);
    private final PublishableKeyQueryPort keyQueries = mock(PublishableKeyQueryPort.class);
    private final IdGenerator ids = mock(IdGenerator.class);
    private final TokenStub tokens = new TokenStub();
    private final Clock clock = Clock.fixed(now, ZoneOffset.UTC);
    private final PublishableKeyManagementPolicy policy = new PublishableKeyManagementPolicy();
    private final CreatePublishableKeyService create =
            new CreatePublishableKeyService(channels, keys, tokens, ids, clock, policy);
    private final RevokePublishableKeyService revoke =
            new RevokePublishableKeyService(channels, keys, clock, policy);
    private final ListPublishableKeysService list =
            new ListPublishableKeysService(channelQueries, keyQueries, ids, policy);
    private final ResolvePublishableKeyService resolve = new ResolvePublishableKeyService(keyQueries, tokens);

    @BeforeEach
    void setUp() {
        when(ids.generateId()).thenReturn(keyId);
        when(ids.convertIdFrom("merchant-1")).thenReturn(merchantId);
    }

    @Test
    void create_disabledOwnedChannel_issuesTokenOnceAndPersistsOnlyDigest() {
        SalesChannel channel = ownedChannel();
        channel.disable(now);
        when(channels.findById(channelId)).thenReturn(Optional.of(channel));
        when(keys.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        CreatePublishableKeyCommand command = new CreatePublishableKeyCommand(channelId, " Web ", access);

        CreatePublishableKeyResult result = create.execute(command);

        ArgumentCaptor<PublishableKey> saved = ArgumentCaptor.forClass(PublishableKey.class);
        verify(keys).save(saved.capture());
        assertThat(saved.getValue().getTokenDigest()).isEqualTo(digest);
        assertThat(saved.getValue().getCreatedAt()).isEqualTo(now);
        assertThat(result.rawToken()).isEqualTo(rawToken);
        assertThat(result.label()).isEqualTo("Web");
        assertThat(result.toString()).doesNotContain(rawToken);
        assertThat(tokens.generateCalls).isEqualTo(1);
    }

    @Test
    void create_otherMerchant_rejectsBeforeGeneratingTokenOrWriting() {
        when(channels.findById(channelId)).thenReturn(Optional.of(ownedChannel()));
        PublishableKeyManagementAccess other = new PublishableKeyManagementAccess(new CommonId("merchant-2"), false);
        CreatePublishableKeyCommand command = new CreatePublishableKeyCommand(channelId, "Web", other);

        assertThatThrownBy(() -> create.execute(command)).isInstanceOf(SalesChannelDomainException.class);
        assertThat(tokens.generateCalls).isZero();
        verifyNoInteractions(keys);
    }

    @Test
    void create_missingChannel_rejectsBeforeTokenGeneration() {
        when(channels.findById(channelId)).thenReturn(Optional.empty());
        CreatePublishableKeyCommand command = new CreatePublishableKeyCommand(channelId, "Web", access);

        assertThatThrownBy(() -> create.execute(command))
                .isInstanceOf(SalesChannelServiceException.class);
        assertThat(tokens.generateCalls).isZero();
        verifyNoInteractions(keys);
    }

    @Test
    void create_duplicateDigest_raisesConflictWithoutSaving() {
        when(channels.findById(channelId)).thenReturn(Optional.of(ownedChannel()));
        when(keys.existsByDigest(digest)).thenReturn(true);
        CreatePublishableKeyCommand command = new CreatePublishableKeyCommand(channelId, "Web", access);

        assertThatThrownBy(() -> create.execute(command))
                .isInstanceOfSatisfying(SalesChannelServiceException.class,
                        exception -> assertThat(exception.getMessageSource().kind()).isEqualTo(ErrorCategory.CONFLICT));
        verify(keys, never()).save(any());
    }

    @Test
    void create_platformChannel_requiresOperator() {
        SalesChannel marketplace = SalesChannel.createMarketplace(channelId, "Marketplace", now);
        when(channels.findById(channelId)).thenReturn(Optional.of(marketplace));
        CreatePublishableKeyCommand merchantCommand = new CreatePublishableKeyCommand(channelId, "Web", access);
        assertThatThrownBy(() -> create.execute(merchantCommand)).isInstanceOf(SalesChannelDomainException.class);

        PublishableKeyManagementAccess administrator = new PublishableKeyManagementAccess(null, true);
        CreatePublishableKeyCommand adminCommand = new CreatePublishableKeyCommand(channelId, "Market", administrator);
        when(keys.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        CreatePublishableKeyResult result = create.execute(adminCommand);
        assertThat(result.salesChannelId()).isEqualTo(channelId);
    }

    @Test
    void revoke_ownedDisabledChannel_isIdempotentWithoutSecondWrite() {
        SalesChannel channel = ownedChannel();
        channel.disable(now);
        PublishableKey key = key(channelId);
        key.pullEvents();
        when(channels.findById(channelId)).thenReturn(Optional.of(channel));
        when(keys.findById(keyId)).thenReturn(Optional.of(key));
        RevokePublishableKeyCommand command = new RevokePublishableKeyCommand(channelId, keyId, access);

        revoke.execute(command);
        revoke.execute(command);

        verify(keys).save(key);
        assertThat(key.getRevokedAt()).isEqualTo(now);
        assertThat(key.getEvents()).hasSize(1);
    }

    @Test
    void revoke_mismatchedChannel_returnsNotFoundWithoutMutation() {
        when(channels.findById(channelId)).thenReturn(Optional.of(ownedChannel()));
        Id otherChannelId = new CommonId("channel-2");
        PublishableKey key = key(otherChannelId);
        when(keys.findById(keyId)).thenReturn(Optional.of(key));
        RevokePublishableKeyCommand command = new RevokePublishableKeyCommand(channelId, keyId, access);

        assertThatThrownBy(() -> revoke.execute(command))
                .isInstanceOfSatisfying(SalesChannelServiceException.class,
                        exception -> assertThat(exception.getMessageSource().kind()).isEqualTo(ErrorCategory.NOT_FOUND));
        assertThat(key.isActive()).isTrue();
        verify(keys, never()).save(any());
    }

    @Test
    void revoke_otherMerchant_rejectsBeforeReadingKey() {
        when(channels.findById(channelId)).thenReturn(Optional.of(ownedChannel()));
        PublishableKeyManagementAccess other = new PublishableKeyManagementAccess(new CommonId("merchant-2"), false);
        RevokePublishableKeyCommand command = new RevokePublishableKeyCommand(channelId, keyId, other);

        assertThatThrownBy(() -> revoke.execute(command)).isInstanceOf(SalesChannelDomainException.class);
        verifyNoInteractions(keys);
    }

    @Test
    void revoke_missingKey_returnsNotFound() {
        when(channels.findById(channelId)).thenReturn(Optional.of(ownedChannel()));
        when(keys.findById(keyId)).thenReturn(Optional.empty());
        RevokePublishableKeyCommand command = new RevokePublishableKeyCommand(channelId, keyId, access);

        assertThatThrownBy(() -> revoke.execute(command)).isInstanceOf(SalesChannelServiceException.class);
        verify(keys, never()).save(any());
    }

    @Test
    void list_ownedDisabledChannel_returnsMetadataPageUsingReadPorts() {
        when(channelQueries.findById("channel-1")).thenReturn(Optional.of(channelView(ChannelStatus.DISABLED)));
        Pageable pageable = PageRequest.of(0, 1);
        PublishableKeyView view = new PublishableKeyView(keyId, channelId, "Web", "pk_aaaaaaaa", now, null);
        Page<PublishableKeyView> page = new PageImpl<>(List.of(view), pageable, 2);
        when(keyQueries.list(channelId, pageable)).thenReturn(page);
        ListPublishableKeysQuery query = new ListPublishableKeysQuery(channelId, access, pageable);

        Page<PublishableKeyView> result = list.execute(query);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).containsExactly(view);
        verifyNoInteractions(channels, keys);
    }

    @Test
    void list_otherMerchant_rejectsBeforeReadingKeys() {
        when(channelQueries.findById("channel-1")).thenReturn(Optional.of(channelView(ChannelStatus.ENABLED)));
        PublishableKeyManagementAccess other = new PublishableKeyManagementAccess(new CommonId("merchant-2"), false);
        ListPublishableKeysQuery query = new ListPublishableKeysQuery(channelId, other, PageRequest.of(0, 20));

        assertThatThrownBy(() -> list.execute(query)).isInstanceOf(SalesChannelDomainException.class);
        verifyNoInteractions(keyQueries, channels, keys);
    }

    @Test
    void list_missingChannel_returnsNotFoundBeforeReadingKeys() {
        when(channelQueries.findById("channel-1")).thenReturn(Optional.empty());
        ListPublishableKeysQuery query = new ListPublishableKeysQuery(channelId, access, PageRequest.of(0, 20));

        assertThatThrownBy(() -> list.execute(query)).isInstanceOf(SalesChannelServiceException.class);
        verifyNoInteractions(keyQueries);
    }

    @Test
    void resolve_malformedTokens_returnsEmptyWithoutHashingOrQuerying() {
        List<String> malformed = Arrays.asList(null, "", "pk_short", rawToken + "=", " " + rawToken,
                "pk_live_" + "a".repeat(43));
        for (String token : malformed) {
            ResolvePublishableKeyQuery query = new ResolvePublishableKeyQuery(token);
            assertThat(resolve.execute(query)).isEmpty();
        }
        assertThat(tokens.digestCalls).isZero();
        verifyNoInteractions(keyQueries);
    }

    @Test
    void resolve_unknownOrRevokedKey_returnsEmpty() {
        when(keyQueries.findActiveByDigest(digest)).thenReturn(Optional.empty());
        ResolvePublishableKeyQuery query = new ResolvePublishableKeyQuery(rawToken);

        assertThat(resolve.execute(query)).isEmpty();
        assertThat(tokens.digestCalls).isEqualTo(1);
        verifyNoInteractions(channels, keys);
    }

    @Test
    void resolve_activeKeyOnDisabledChannel_preservesDisabledStatus() {
        ResolvedPublishableKeyView view = new ResolvedPublishableKeyView(channelId, false, true);
        when(keyQueries.findActiveByDigest(digest)).thenReturn(Optional.of(view));
        ResolvePublishableKeyQuery query = new ResolvePublishableKeyQuery(rawToken);

        assertThat(resolve.execute(query)).contains(view);
        assertThat(query.toString()).doesNotContain(rawToken);
    }

    @Test
    void resolve_inactiveProjection_failsClosed() {
        ResolvedPublishableKeyView view = new ResolvedPublishableKeyView(channelId, true, false);
        when(keyQueries.findActiveByDigest(digest)).thenReturn(Optional.of(view));
        ResolvePublishableKeyQuery query = new ResolvePublishableKeyQuery(rawToken);

        assertThat(resolve.execute(query)).isEmpty();
    }

    private SalesChannel ownedChannel() {
        return SalesChannel.createWebsite(channelId, merchantId, "Website", now);
    }

    private SalesChannelView channelView(ChannelStatus status) {
        return new SalesChannelView("channel-1", "Website", ChannelType.WEBSITE, ChannelOwner.SELLER,
                "merchant-1", status, now, now, 0);
    }

    private PublishableKey key(Id salesChannelId) {
        return PublishableKey.create(keyId, salesChannelId, "Web", digest, "pk_aaaaaaaa", now);
    }

    private class TokenStub implements PublishableTokenGeneratorPort {
        private int generateCalls;
        private int digestCalls;

        @Override
        public GeneratedToken generate() {
            generateCalls++;
            return new GeneratedToken(rawToken, digest, "pk_aaaaaaaa");
        }

        @Override
        public String digest(String token) {
            assertThat(token).isEqualTo(rawToken);
            digestCalls++;
            return digest;
        }
    }
}
