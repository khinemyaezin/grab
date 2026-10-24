package com.grab.discovery.application.service;

import com.grab.discovery.application.exception.StorefrontQueryServiceException;
import com.grab.discovery.application.model.read.ResolveShoppingContextQuery;
import com.grab.discovery.application.model.read.ResolveShoppingContextResult;
import com.grab.discovery.application.policy.ShoppingContextResolutionPolicy;
import com.grab.discovery.application.port.outbound.RegionContextPort;
import com.grab.discovery.application.port.outbound.SalesChannelPublishableKeyPort;
import com.grab.discovery.application.port.outbound.SalesChannelRegionPort;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResolveShoppingContextServiceTest {
    private static final CommonId CHANNEL_ID = new CommonId("channel-1");
    private static final CommonId SG_REGION_ID = new CommonId("region-sg");
    private static final CommonId MY_REGION_ID = new CommonId("region-my");

    @Mock
    private SalesChannelPublishableKeyPort publishableKeys;
    @Mock
    private SalesChannelRegionPort channelRegions;
    @Mock
    private RegionContextPort regions;

    private ResolveShoppingContextService service;

    @BeforeEach
    void setUp() {
        service = new ResolveShoppingContextService(
                publishableKeys,
                channelRegions,
                regions,
                new ShoppingContextResolutionPolicy()
        );
    }

    @Test
    void explicitCountry_returnsContextAndServedState() {
        when(publishableKeys.resolve("pk_token")).thenReturn(Optional.of(
                new SalesChannelPublishableKeyPort.ResolvedChannel(CHANNEL_ID, true)));
        when(regions.findByCountry("SG")).thenReturn(Optional.of(
                new RegionContextPort.RegionView(SG_REGION_ID, "usd", true)));
        when(channelRegions.servesRegion(CHANNEL_ID, SG_REGION_ID)).thenReturn(false);

        ResolveShoppingContextResult result = service.execute(
                new ResolveShoppingContextQuery("pk_token", " sg ", " en-sg "));

        assertThat(result.regionServed()).isFalse();
        assertThat(result.context().salesChannelId()).isEqualTo(CHANNEL_ID);
        assertThat(result.context().regionId()).isEqualTo(SG_REGION_ID);
        assertThat(result.context().countryCode()).isEqualTo("SG");
        assertThat(result.context().currencyCode()).isEqualTo("USD");
        assertThat(result.context().locale()).isEqualTo("en-SG");
        verify(channelRegions, never()).findDefaultRegion(CHANNEL_ID);
    }

    @Test
    void omittedCountry_usesServedDefaultAndLeavesCountryNull() {
        when(publishableKeys.resolve("pk_token")).thenReturn(Optional.of(
                new SalesChannelPublishableKeyPort.ResolvedChannel(CHANNEL_ID, true)));
        when(channelRegions.findDefaultRegion(CHANNEL_ID)).thenReturn(Optional.of(SG_REGION_ID));
        when(regions.findById(SG_REGION_ID)).thenReturn(Optional.of(
                new RegionContextPort.RegionView(SG_REGION_ID, "USD", true)));
        when(channelRegions.servesRegion(CHANNEL_ID, SG_REGION_ID)).thenReturn(true);

        ResolveShoppingContextResult result = service.execute(
                new ResolveShoppingContextQuery("pk_token", null, null));

        assertThat(result.regionServed()).isTrue();
        assertThat(result.context().countryCode()).isNull();
        assertThat(result.context().currencyCode()).isEqualTo("USD");
        assertThat(result.context().locale()).isNull();
    }

    @Test
    void invalidKey_usesGenericErrorAndStopsBeforeRegionalLookups() {
        when(publishableKeys.resolve("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new ResolveShoppingContextQuery("bad", "SG", null)))
                .isInstanceOf(StorefrontQueryServiceException.class)
                .hasMessage("Invalid or revoked publishable key")
                .satisfies(exception -> {
                    StorefrontQueryServiceException serviceException = (StorefrontQueryServiceException) exception;
                    assertThat(serviceException.getMessageSource().code())
                            .isEqualTo("sfq.service.publishable_key.invalid");
                    assertThat(serviceException.getMessageSource().args()).isEmpty();
                });
        verify(regions, never()).findByCountry("SG");
        verify(channelRegions, never()).findDefaultRegion(CHANNEL_ID);
    }

    @Test
    void disabledChannel_failsBeforeRegionalLookups() {
        when(publishableKeys.resolve("pk_token")).thenReturn(Optional.of(
                new SalesChannelPublishableKeyPort.ResolvedChannel(CHANNEL_ID, false)));

        assertThatThrownBy(() -> service.execute(new ResolveShoppingContextQuery("pk_token", "SG", null)))
                .isInstanceOf(StorefrontQueryServiceException.class)
                .satisfies(exception -> {
                    StorefrontQueryServiceException serviceException = (StorefrontQueryServiceException) exception;
                    assertThat(serviceException.getMessageSource().code())
                            .isEqualTo("sfq.service.context.channel_disabled");
                });
        verify(regions, never()).findByCountry("SG");
        verify(channelRegions, never()).findDefaultRegion(CHANNEL_ID);
    }

    @Test
    void invalidCountry_returnsBadRequestWithoutRegionLookup() {
        when(publishableKeys.resolve("pk_token")).thenReturn(Optional.of(
                new SalesChannelPublishableKeyPort.ResolvedChannel(CHANNEL_ID, true)));

        assertThatThrownBy(() -> service.execute(new ResolveShoppingContextQuery("pk_token", "S1", null)))
                .isInstanceOf(StorefrontQueryServiceException.class)
                .satisfies(exception -> {
                    StorefrontQueryServiceException serviceException = (StorefrontQueryServiceException) exception;
                    assertThat(serviceException.getMessageSource().code())
                            .isEqualTo("sfq.service.context.country_invalid");
                });
        verify(regions, never()).findByCountry("S1");
    }

    @Test
    void explicitCountryWithNoActiveRegion_returnsRegionUnavailable() {
        when(publishableKeys.resolve("pk_token")).thenReturn(Optional.of(
                new SalesChannelPublishableKeyPort.ResolvedChannel(CHANNEL_ID, true)));
        when(regions.findByCountry("MY")).thenReturn(Optional.of(
                new RegionContextPort.RegionView(MY_REGION_ID, "MYR", false)));

        assertThatThrownBy(() -> service.execute(new ResolveShoppingContextQuery("pk_token", "MY", null)))
                .isInstanceOf(StorefrontQueryServiceException.class)
                .satisfies(exception -> {
                    StorefrontQueryServiceException serviceException = (StorefrontQueryServiceException) exception;
                    assertThat(serviceException.getMessageSource().code())
                            .isEqualTo("sfq.service.context.region_unavailable");
                });
    }

    @Test
    void providerFailure_isPropagated() {
        RuntimeException failure = new RuntimeException("provider unavailable");
        when(publishableKeys.resolve("pk_token")).thenThrow(failure);

        assertThatThrownBy(() -> service.execute(new ResolveShoppingContextQuery("pk_token", null, null)))
                .isSameAs(failure);
    }
}
