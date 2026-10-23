package com.saleschannel.domain.aggregate;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.saleschannel.domain.event.PublishableKeyCreatedEvent;
import com.saleschannel.domain.event.PublishableKeyRevokedEvent;
import com.saleschannel.domain.exception.SalesChannelDomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PublishableKeyTest {
    private final Id keyId = new CommonId("key-1");
    private final Id channelId = new CommonId("channel-1");
    private final Instant now = Instant.parse("2026-10-23T00:00:00Z");
    private final String digest = "a".repeat(64);

    @Test
    void create_validInput_trimsLabelAndEmitsOnlyLifecycleMetadata() {
        PublishableKey key = create("  Website  ", digest, "pk_abcdefgh");
        List<Event> events = key.pullEvents();

        assertThat(key.getId()).isEqualTo(keyId);
        assertThat(key.getSalesChannelId()).isEqualTo(channelId);
        assertThat(key.getLabel()).isEqualTo("Website");
        assertThat(key.isActive()).isTrue();
        assertThat(events).containsExactly(new PublishableKeyCreatedEvent("key-1", "channel-1", now));
        assertThat(events.toString()).doesNotContain(digest, "pk_abcdefgh");
    }

    @Test
    void create_invalidLabels_rejectsWithoutEchoingInput() {
        List<String> labels = List.of("", "   ", "x".repeat(101));
        for (String label : labels) {
            assertThatThrownBy(() -> create(label, digest, "pk_abcdefgh"))
                    .isInstanceOf(SalesChannelDomainException.class)
                    .hasMessage("Invalid publishable key label");
        }
        assertThatThrownBy(() -> create(null, digest, "pk_abcdefgh"))
                .isInstanceOf(SalesChannelDomainException.class);
        PublishableKey key = create("x".repeat(100), digest, "pk_abcdefgh");
        assertThat(key.getLabel()).hasSize(100);
    }

    @Test
    void create_invalidDigests_rejectsWithoutEchoingTokenMaterial() {
        List<String> digests = List.of("a".repeat(63), "A".repeat(64), "g".repeat(64), "a".repeat(65));
        for (String value : digests) {
            assertThatThrownBy(() -> create("Website", value, "pk_abcdefgh"))
                    .isInstanceOf(SalesChannelDomainException.class)
                    .hasMessage("Invalid publishable key tokenDigest");
        }
        assertThatThrownBy(() -> create("Website", null, "pk_abcdefgh"))
                .isInstanceOf(SalesChannelDomainException.class);
    }

    @Test
    void create_invalidDisplayPrefix_rejectsFullTokenOrWrongFormat() {
        List<String> prefixes = List.of("pk_live_abcdefgh", "pk_short", "pk_abcdefghTOO_LONG", "abcdefgh");
        for (String prefix : prefixes) {
            assertThatThrownBy(() -> create("Website", digest, prefix))
                    .isInstanceOf(SalesChannelDomainException.class);
        }
    }

    @Test
    void revoke_repeatedCall_preservesTimestampAndSingleEvent() {
        PublishableKey key = create("Website", digest, "pk_abcdefgh");
        key.pullEvents();
        Instant revokedAt = now.plusSeconds(60);
        key.revoke(revokedAt);
        key.revoke(now.plusSeconds(120));

        assertThat(key.isActive()).isFalse();
        assertThat(key.getRevokedAt()).isEqualTo(revokedAt);
        assertThat(key.pullEvents()).containsExactly(
                new PublishableKeyRevokedEvent("key-1", "channel-1", revokedAt));
    }

    @Test
    void revoke_beforeCreation_rejectsWithoutChangingState() {
        PublishableKey key = create("Website", digest, "pk_abcdefgh");
        key.pullEvents();
        Instant before = now.minusSeconds(1);

        assertThatThrownBy(() -> key.revoke(before)).isInstanceOf(SalesChannelDomainException.class);
        assertThat(key.isActive()).isTrue();
        assertThat(key.getEvents()).isEmpty();
    }

    @Test
    void constructor_restoredKey_hasNoEventsAndCannotBeUnrevoked() {
        PublishableKey key = new PublishableKey(
                keyId, channelId, "Website", digest, "pk_abcdefgh", now, now);
        key.revoke(now.plusSeconds(60));

        assertThat(key.getEvents()).isEmpty();
        assertThat(key.getRevokedAt()).isEqualTo(now);
        assertThat(key.getSalesChannelId()).isEqualTo(channelId);
    }

    @Test
    void constructor_invalidLifecycle_rejects() {
        Instant before = now.minusSeconds(1);
        assertThatThrownBy(() -> new PublishableKey(
                keyId, channelId, "Website", digest, "pk_abcdefgh", now, before))
                .isInstanceOf(SalesChannelDomainException.class);
    }

    private PublishableKey create(String label, String tokenDigest, String prefix) {
        return PublishableKey.create(keyId, channelId, label, tokenDigest, prefix, now);
    }
}
