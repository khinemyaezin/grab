package com.grab.framework.logger.internal;

import com.grab.framework.logger.console.ConsoleLoggerProvider;
import com.grab.framework.logger.slf4j.Slf4jLoggerProvider;
import com.grab.framework.logger.spi.LoggerProvider;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public final class LoggerProviderRegistry {

    private final List<LoggerProvider> providers;

    public LoggerProviderRegistry() {
        this(loadProviders());
    }

    public LoggerProviderRegistry(List<LoggerProvider> providers) {
        this.providers = providers.stream()
                .sorted(Comparator
                        .comparingInt(LoggerProvider::priority)
                        .reversed()
                        .thenComparing(LoggerProvider::id))
                .toList();
    }

    public List<LoggerProvider> all() {
        return providers;
    }

    public Optional<LoggerProvider> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return providers.stream()
                .filter(provider -> provider.id().equalsIgnoreCase(id.trim()))
                .findFirst();
    }

    public Optional<LoggerProvider> highestPriorityAvailable() {
        return providers.stream()
                .filter(LoggerProvider::isAvailable)
                .findFirst();
    }

    private static List<LoggerProvider> loadProviders() {
        List<LoggerProvider> discovered = ServiceLoader.load(LoggerProvider.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .toList();

        Map<String, LoggerProvider> unique = Stream.concat(
                        builtInProviders().stream(),
                        discovered.stream()
                )
                .collect(
                        LinkedHashMap::new,
                        (map, provider) -> map.merge(
                                normalizeId(provider.id()),
                                provider,
                                LoggerProviderRegistry::selectPreferred
                        ),
                        LinkedHashMap::putAll
                );

        return unique.values().stream()
                .sorted(Comparator
                        .comparingInt(LoggerProvider::priority)
                        .reversed()
                        .thenComparing(LoggerProvider::id))
                .toList();
    }

    private static List<LoggerProvider> builtInProviders() {
        return List.of(
                new Slf4jLoggerProvider(),
                new ConsoleLoggerProvider()
        );
    }

    private static LoggerProvider selectPreferred(LoggerProvider current, LoggerProvider incoming) {
        int priorityCompare = Integer.compare(incoming.priority(), current.priority());
        if (priorityCompare > 0) {
            return incoming;
        }
        if (priorityCompare < 0) {
            return current;
        }

        String currentClass = current.getClass().getName();
        String incomingClass = incoming.getClass().getName();
        return incomingClass.compareTo(currentClass) < 0 ? incoming : current;
    }

    private static String normalizeId(String id) {
        if (id == null) {
            return "";
        }
        return id.trim().toLowerCase(Locale.ROOT);
    }
}
