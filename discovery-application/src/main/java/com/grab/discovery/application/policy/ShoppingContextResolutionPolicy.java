package com.grab.discovery.application.policy;

import java.util.Currency;
import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class ShoppingContextResolutionPolicy {
    private static final Pattern COUNTRY_PATTERN = Pattern.compile("[A-Za-z]{2}");
    private static final Pattern CURRENCY_PATTERN = Pattern.compile("[A-Za-z]{3}");
    private static final Set<String> ISO_COUNTRIES = Set.of(Locale.getISOCountries());

    public String normalizeCountry(String countryCode) {
        if (countryCode == null) {
            return null;
        }
        String trimmed = countryCode.trim();
        if (!COUNTRY_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("country");
        }
        String normalized = trimmed.toUpperCase(Locale.ROOT);
        if (!ISO_COUNTRIES.contains(normalized)) {
            throw new IllegalArgumentException("country");
        }
        return normalized;
    }

    public String normalizeLocale(String locale) {
        if (locale == null) {
            return null;
        }
        String trimmed = locale.trim();
        if (trimmed.isEmpty() || trimmed.contains("*") || trimmed.contains(",")) {
            throw new IllegalArgumentException("locale");
        }
        try {
            Locale parsed = new Locale.Builder().setLanguageTag(trimmed).build();
            String languageTag = parsed.toLanguageTag();
            if ("und".equals(languageTag)) {
                throw new IllegalArgumentException("locale");
            }
            return languageTag;
        } catch (IllformedLocaleException exception) {
            throw new IllegalArgumentException("locale", exception);
        }
    }

    public String normalizeCurrency(String currencyCode) {
        if (currencyCode == null) {
            throw new IllegalArgumentException("currency");
        }
        String normalized = currencyCode.trim().toUpperCase(Locale.ROOT);
        if (!CURRENCY_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("currency");
        }
        try {
            Currency.getInstance(normalized);
            return normalized;
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("currency", exception);
        }
    }
}
