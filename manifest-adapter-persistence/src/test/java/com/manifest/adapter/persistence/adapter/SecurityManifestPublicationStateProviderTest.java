package com.manifest.adapter.persistence.adapter;

import com.manifest.adapter.persistence.entity.SecurityManifestPublicationState;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityManifestPublicationStateProviderTest {

    private static class TestState extends SecurityManifestPublicationState {
        TestState(String moduleKey) {
            super(moduleKey);
        }
    }

    @Test
    void optimisticProvider_whenStateExists_returnsManagedEntity() {
        TestState existing = new TestState("inventory");
        Map<String, TestState> states = new HashMap<>();
        states.put("inventory", existing);
        List<TestState> persisted = new ArrayList<>();
        AtomicInteger flushCount = new AtomicInteger();
        EntityManager entityManager = entityManager(states, persisted, flushCount, null);

        Function<String, TestState> provider = SecurityManifestPublicationStateProvider.optimisticProvider(
                entityManager, TestState.class, TestState::new);

        TestState result = provider.apply("inventory");

        assertSame(existing, result);
        assertTrue(persisted.isEmpty());
        assertEquals(0, flushCount.get());
    }

    @Test
    void optimisticProvider_whenStateIsMissing_persistsAndFlushesWithinCurrentTransaction() {
        TestState created = new TestState("inventory");
        Map<String, TestState> states = new HashMap<>();
        List<TestState> persisted = new ArrayList<>();
        AtomicInteger flushCount = new AtomicInteger();
        EntityManager entityManager = entityManager(states, persisted, flushCount, null);

        Function<String, TestState> provider = SecurityManifestPublicationStateProvider.optimisticProvider(
                entityManager, TestState.class, moduleKey -> created);

        TestState result = provider.apply("inventory");

        assertSame(created, result);
        assertEquals(List.of(created), persisted);
        assertEquals(1, flushCount.get());
    }

    @Test
    void optimisticProvider_whenFlushFails_propagatesFailureForTransactionRetry() {
        TestState created = new TestState("inventory");
        Map<String, TestState> states = new HashMap<>();
        List<TestState> persisted = new ArrayList<>();
        AtomicInteger flushCount = new AtomicInteger();
        PersistenceException flushFailure = new PersistenceException("insert failed");
        EntityManager entityManager = entityManager(states, persisted, flushCount, flushFailure);

        Function<String, TestState> provider = SecurityManifestPublicationStateProvider.optimisticProvider(
                entityManager, TestState.class, moduleKey -> created);

        PersistenceException thrown = assertThrows(PersistenceException.class, () -> provider.apply("inventory"));

        assertSame(flushFailure, thrown);
        assertEquals(List.of(created), persisted);
        assertEquals(1, flushCount.get());
    }

    private EntityManager entityManager(Map<String, TestState> states, List<TestState> persisted,
                                        AtomicInteger flushCount, PersistenceException flushFailure) {
        InvocationHandler invocationHandler = (proxy, method, arguments) -> switch (method.getName()) {
            case "find" -> states.get(arguments[1]);
            case "persist" -> {
                TestState state = (TestState) arguments[0];
                persisted.add(state);
                states.put(state.moduleKey(), state);
                yield null;
            }
            case "flush" -> {
                flushCount.incrementAndGet();
                if (flushFailure != null) {
                    throw flushFailure;
                }
                yield null;
            }
            default -> throw new UnsupportedOperationException(method.getName());
        };
        return (EntityManager) Proxy.newProxyInstance(EntityManager.class.getClassLoader(),
                new Class<?>[]{EntityManager.class}, invocationHandler);
    }
}
