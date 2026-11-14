package com.grab.outbox.infrastructure.claim;

import org.springframework.core.env.PropertyResolver;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Builds the configured claim strategy. Selection is manual, per module: no database detection.
 *
 * <pre>
 * catalog:
 *   outbox:
 *     claim-strategy: skip-locked   # skip-locked (default) | conditional-update
 * </pre>
 */
public final class OutboxClaimStrategies {

    public static final String PROPERTY_SUFFIX = ".claim-strategy";
    public static final Type DEFAULT_TYPE = Type.SKIP_LOCKED;

    public enum Type {
        /**
         * {@link SkipLockedClaimStrategy}: PostgreSQL, MySQL ≥ 8.0, MariaDB ≥ 10.6, Oracle, SQL Server, DB2.
         */
        SKIP_LOCKED(SkipLockedClaimStrategy.NAME),
        /**
         * {@link ConditionalUpdateClaimStrategy}: any JPA database, incl. H2, MySQL 5.7, MariaDB &lt; 10.6.
         */
        CONDITIONAL_UPDATE(ConditionalUpdateClaimStrategy.NAME);

        private final String configValue;

        Type(String configValue) {
            this.configValue = configValue;
        }

        public String configValue() {
            return configValue;
        }

        /**
         * Accepts the config value ({@code skip-locked}) and the enum name ({@code SKIP_LOCKED}), case-insensitive.
         */
        public static Type parse(String value) {
            String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT).replace('_', '-');
            return Arrays.stream(values())
                    .filter(type -> type.configValue.equals(normalized))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unknown outbox claim strategy '" + value + "'; expected one of " + allowedValues()));
        }

        private static String allowedValues() {
            return Arrays.stream(values()).map(Type::configValue).collect(Collectors.joining(", "));
        }
    }

    private OutboxClaimStrategies() {
    }

    public static OutboxClaimStrategy of(Type type) {
        return switch (Objects.requireNonNull(type, "type")) {
            case SKIP_LOCKED -> new SkipLockedClaimStrategy();
            case CONDITIONAL_UPDATE -> new ConditionalUpdateClaimStrategy();
        };
    }

    public static OutboxClaimStrategy defaultStrategy() {
        return of(DEFAULT_TYPE);
    }

    /**
     * Reads {@code <prefix>.claim-strategy}, e.g. {@code fromProperties(env, "catalog.outbox")}.
     *
     * @throws IllegalArgumentException for an unknown value, so a typo fails at startup
     */
    public static OutboxClaimStrategy fromProperties(PropertyResolver properties, String prefix) {
        String key = prefix + PROPERTY_SUFFIX;
        String value = properties.getProperty(key, DEFAULT_TYPE.configValue());
        try {
            Type type = Type.parse(value);
            return of(type);
        } catch (IllegalArgumentException invalid) {
            throw new IllegalArgumentException(key + ": " + invalid.getMessage(), invalid);
        }
    }
}
