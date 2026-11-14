package com.grab.app.shared.scheduling;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Lock provider behind {@link ShedLockSingletonJobExecutor}, selected per module with
 * {@code <module>.scheduling.lock-provider}.
 */
public enum LockProviderType {

    /**
     * ShedLock JDBC table with database time. Default; works on every database ShedLock supports.
     */
    JDBC("jdbc"),

    /**
     * ShedLock JDBC table with application time, for databases whose current time ShedLock cannot read.
     * Requires NTP on all pods and a {@code lockAtMostFor} well above the maximum clock skew.
     */
    JDBC_APP_TIME("jdbc-app-time");

    private final String configValue;

    LockProviderType(String configValue) {
        this.configValue = configValue;
    }

    public String configValue() {
        return configValue;
    }

    /**
     * Accepts the config value ({@code jdbc-app-time}) and the enum name ({@code JDBC_APP_TIME}), case-insensitive.
     */
    public static LockProviderType parse(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT).replace('_', '-');
        return Arrays.stream(values())
                .filter(type -> type.configValue.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown lock provider '" + value + "'; expected one of " + allowedValues()));
    }

    private static String allowedValues() {
        return Arrays.stream(values()).map(LockProviderType::configValue).collect(Collectors.joining(", "));
    }
}
