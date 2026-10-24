package com.grab.discovery.application.service;

import com.grab.discovery.application.exception.StorefrontQueryServiceError;
import com.grab.discovery.application.exception.StorefrontQueryServiceException;
import com.grab.discovery.application.model.read.ResolveShoppingContextQuery;
import com.grab.discovery.application.model.read.ResolveShoppingContextResult;
import com.grab.discovery.application.model.read.ShoppingContext;
import com.grab.discovery.application.policy.ShoppingContextResolutionPolicy;
import com.grab.discovery.application.port.inbound.ResolveShoppingContextUseCase;
import com.grab.discovery.application.port.outbound.RegionContextPort;
import com.grab.discovery.application.port.outbound.SalesChannelPublishableKeyPort;
import com.grab.discovery.application.port.outbound.SalesChannelRegionPort;
import com.grab.framework.id.Id;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class ResolveShoppingContextService implements ResolveShoppingContextUseCase {
    private static final String INVALID_KEY_MESSAGE = "Invalid or revoked publishable key";

    private final SalesChannelPublishableKeyPort publishableKeys;
    private final SalesChannelRegionPort channelRegions;
    private final RegionContextPort regions;
    private final ShoppingContextResolutionPolicy policy;

    @Override
    public ResolveShoppingContextResult execute(ResolveShoppingContextQuery query) {
        SalesChannelPublishableKeyPort.ResolvedChannel channel = resolveChannel(query.publishableKey());
        String countryCode = normalizeCountry(query.countryCode());
        String locale = normalizeLocale(query.locale());
        if (countryCode != null) {
            return resolveExplicitCountry(channel.salesChannelId(), countryCode, locale);
        }
        return resolveDefaultRegion(channel.salesChannelId(), locale);
    }

    private SalesChannelPublishableKeyPort.ResolvedChannel resolveChannel(String rawToken) {
        Optional<SalesChannelPublishableKeyPort.ResolvedChannel> resolved = publishableKeys.resolve(rawToken);
        SalesChannelPublishableKeyPort.ResolvedChannel channel = resolved.orElseThrow(this::invalidKey);
        if (channel.salesChannelId() == null) {
            throw new IllegalStateException("Publishable key provider returned no channel identifier");
        }
        if (!channel.enabled()) {
            throw exception(new StorefrontQueryServiceError.ContextChannelDisabled(), "Sales channel is disabled");
        }
        return channel;
    }

    private ResolveShoppingContextResult resolveExplicitCountry(Id channelId, String countryCode, String locale) {
        Optional<RegionContextPort.RegionView> regionResult = regions.findByCountry(countryCode);
        RegionContextPort.RegionView region = regionResult.orElseThrow(this::regionUnavailable);
        validateActiveRegion(region);
        String currencyCode = normalizeCurrency(region.currencyCode(), false);
        boolean regionServed = channelRegions.servesRegion(channelId, region.regionId());
        ShoppingContext context = new ShoppingContext(
                channelId,
                region.regionId(),
                countryCode,
                currencyCode,
                locale
        );
        return new ResolveShoppingContextResult(context, regionServed);
    }

    private ResolveShoppingContextResult resolveDefaultRegion(Id channelId, String locale) {
        Optional<Id> defaultRegionResult = channelRegions.findDefaultRegion(channelId);
        Id defaultRegionId = defaultRegionResult.orElseThrow(this::defaultRegionUnavailable);
        Optional<RegionContextPort.RegionView> regionResult = regions.findById(defaultRegionId);
        RegionContextPort.RegionView region = regionResult.orElseThrow(this::defaultRegionUnavailable);
        validateDefaultRegion(defaultRegionId, region);
        String currencyCode = normalizeCurrency(region.currencyCode(), true);
        boolean regionServed = channelRegions.servesRegion(channelId, defaultRegionId);
        if (!regionServed) {
            throw defaultRegionUnavailable();
        }
        ShoppingContext context = new ShoppingContext(
                channelId,
                defaultRegionId,
                null,
                currencyCode,
                locale
        );
        return new ResolveShoppingContextResult(context, true);
    }

    private void validateDefaultRegion(Id expectedRegionId, RegionContextPort.RegionView region) {
        if (region.regionId() == null) {
            throw new IllegalStateException("Region provider returned no region identifier");
        }
        boolean matchesConfiguredRegion = expectedRegionId.getValue().equals(region.regionId().getValue());
        if (!matchesConfiguredRegion || !region.active()) {
            throw defaultRegionUnavailable();
        }
    }

    private void validateActiveRegion(RegionContextPort.RegionView region) {
        if (region.regionId() == null) {
            throw new IllegalStateException("Region provider returned no region identifier");
        }
        if (!region.active()) {
            throw regionUnavailable();
        }
    }

    private String normalizeCountry(String rawCountry) {
        try {
            return policy.normalizeCountry(rawCountry);
        } catch (IllegalArgumentException exception) {
            throw exception(new StorefrontQueryServiceError.CountryCodeInvalid(), "Country code is invalid");
        }
    }

    private String normalizeLocale(String rawLocale) {
        try {
            return policy.normalizeLocale(rawLocale);
        } catch (IllegalArgumentException exception) {
            throw exception(new StorefrontQueryServiceError.LocaleInvalid(), "Locale is invalid");
        }
    }

    private String normalizeCurrency(String rawCurrency, boolean defaultRegion) {
        try {
            return policy.normalizeCurrency(rawCurrency);
        } catch (IllegalArgumentException exception) {
            if (defaultRegion) {
                throw defaultRegionUnavailable();
            }
            throw regionUnavailable();
        }
    }

    private StorefrontQueryServiceException invalidKey() {
        return exception(new StorefrontQueryServiceError.InvalidPublishableKey(), INVALID_KEY_MESSAGE);
    }

    private StorefrontQueryServiceException regionUnavailable() {
        return exception(new StorefrontQueryServiceError.RegionUnavailable(), "No active Region is configured");
    }

    private StorefrontQueryServiceException defaultRegionUnavailable() {
        return exception(new StorefrontQueryServiceError.DefaultRegionUnavailable(), "The channel default Region is unavailable");
    }

    private StorefrontQueryServiceException exception(StorefrontQueryServiceError error, String message) {
        return new StorefrontQueryServiceException(error, message);
    }
}
