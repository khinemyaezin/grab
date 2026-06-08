package com.inventory.domain.aggregate;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.inventory.domain.enums.ZoneType;
import com.inventory.domain.event.ZoneActivatedEvent;
import com.inventory.domain.event.ZoneCreatedEvent;
import com.inventory.domain.event.ZoneDeactivatedEvent;
import com.inventory.domain.event.ZoneUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZoneTest {

    private static final Id ZONE_ID = id("zone-1");
    private static final Id LOCATION_ID = id("loc-1");
    private static final String CODE = "ZONE-P1";
    private static final String NAME = "Picking Zone A";
    private static final ZoneType TYPE = ZoneType.PICKING;

    private Zone zone;

    @BeforeEach
    void setUp() {
        zone = Zone.create(ZONE_ID, LOCATION_ID, CODE, NAME, TYPE);
    }

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        void shouldInitializeWithRequiredFields() {
            assertThat(zone.getId()).isEqualTo(ZONE_ID);
            assertThat(zone.getLocationId()).isEqualTo(LOCATION_ID);
            assertThat(zone.getCode()).isEqualTo(CODE);
            assertThat(zone.getName()).isEqualTo(NAME);
            assertThat(zone.getType()).isEqualTo(TYPE);
            assertThat(zone.isActive()).isTrue();
        }

        @Test
        void shouldEmitZoneCreatedEvent() {
            List<Event> events = zone.getEvents();

            assertThat(events).hasSize(1);
            assertThat(events.getFirst()).isInstanceOf(ZoneCreatedEvent.class);
            ZoneCreatedEvent event = (ZoneCreatedEvent) events.getFirst();
            assertThat(event.zoneId()).isEqualTo(ZONE_ID);
            assertThat(event.locationId()).isEqualTo(LOCATION_ID);
            assertThat(event.code()).isEqualTo(CODE);
            assertThat(event.name()).isEqualTo(NAME);
            assertThat(event.type()).isEqualTo(TYPE);
        }

        @Test
        void withNullLocationId_shouldThrow() {
            assertThatThrownBy(() -> Zone.create(ZONE_ID, null, CODE, NAME, TYPE))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("locationId is required");
        }

        @Test
        void withNullCode_shouldThrow() {
            assertThatThrownBy(() -> Zone.create(ZONE_ID, LOCATION_ID, null, NAME, TYPE))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("code is required");
        }

        @Test
        void withNullName_shouldThrow() {
            assertThatThrownBy(() -> Zone.create(ZONE_ID, LOCATION_ID, CODE, null, TYPE))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("name is required");
        }

        @Test
        void withNullType_shouldThrow() {
            assertThatThrownBy(() -> Zone.create(ZONE_ID, LOCATION_ID, CODE, NAME, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("type is required");
        }
    }

    @Nested
    @DisplayName("updateMetadata")
    class UpdateMetadataTests {

        @Test
        void shouldUpdateAllFields_whenAllProvided() {
            zone.pullEvents();

            zone.updateMetadata("ZONE-P2", "Picking Zone B", ZoneType.STORAGE);

            assertThat(zone.getCode()).isEqualTo("ZONE-P2");
            assertThat(zone.getName()).isEqualTo("Picking Zone B");
            assertThat(zone.getType()).isEqualTo(ZoneType.STORAGE);
        }

        @Test
        void shouldKeepExistingCode_whenCodeIsNull() {
            zone.pullEvents();

            zone.updateMetadata(null, "Updated Name", ZoneType.STORAGE);

            assertThat(zone.getCode()).isEqualTo(CODE);
            assertThat(zone.getName()).isEqualTo("Updated Name");
            assertThat(zone.getType()).isEqualTo(ZoneType.STORAGE);
        }

        @Test
        void shouldKeepExistingName_whenNameIsNull() {
            zone.pullEvents();

            zone.updateMetadata("ZONE-P2", null, ZoneType.STORAGE);

            assertThat(zone.getCode()).isEqualTo("ZONE-P2");
            assertThat(zone.getName()).isEqualTo(NAME);
            assertThat(zone.getType()).isEqualTo(ZoneType.STORAGE);
        }

        @Test
        void shouldKeepExistingType_whenTypeIsNull() {
            zone.pullEvents();

            zone.updateMetadata("ZONE-P2", "Picking Zone B", null);

            assertThat(zone.getCode()).isEqualTo("ZONE-P2");
            assertThat(zone.getName()).isEqualTo("Picking Zone B");
            assertThat(zone.getType()).isEqualTo(TYPE);
        }

        @Test
        void shouldDoNothing_whenAllNull() {
            zone.pullEvents();

            zone.updateMetadata(null, null, null);

            assertThat(zone.getCode()).isEqualTo(CODE);
            assertThat(zone.getName()).isEqualTo(NAME);
            assertThat(zone.getType()).isEqualTo(TYPE);
        }

        @Test
        void shouldEmitZoneUpdatedEvent() {
            zone.pullEvents();

            zone.updateMetadata("ZONE-P2", "Picking Zone B", ZoneType.STORAGE);

            List<Event> events = zone.getEvents();
            assertThat(events).hasSize(1);
            assertThat(events.getFirst()).isInstanceOf(ZoneUpdatedEvent.class);
            ZoneUpdatedEvent event = (ZoneUpdatedEvent) events.getFirst();
            assertThat(event.zoneId()).isEqualTo(ZONE_ID);
            assertThat(event.locationId()).isEqualTo(LOCATION_ID);
            assertThat(event.code()).isEqualTo("ZONE-P2");
            assertThat(event.name()).isEqualTo("Picking Zone B");
            assertThat(event.type()).isEqualTo(ZoneType.STORAGE);
        }

        @Test
        void shouldEmitZoneUpdatedEvent_evenWhenAllNull() {
            zone.pullEvents();

            zone.updateMetadata(null, null, null);

            List<Event> events = zone.getEvents();
            assertThat(events).hasSize(1);
            assertThat(events.getFirst()).isInstanceOf(ZoneUpdatedEvent.class);
            ZoneUpdatedEvent event = (ZoneUpdatedEvent) events.getFirst();
            assertThat(event.code()).isEqualTo(CODE);
            assertThat(event.name()).isEqualTo(NAME);
            assertThat(event.type()).isEqualTo(TYPE);
        }
    }

    @Nested
    @DisplayName("activate")
    class ActivateTests {

        @Test
        void shouldSetActiveToTrue() {
            Zone inactiveZone = new Zone(ZONE_ID, LOCATION_ID, CODE, NAME, TYPE, false);

            inactiveZone.activate();

            assertThat(inactiveZone.isActive()).isTrue();
        }

        @Test
        void shouldBeIdempotent() {
            zone.activate();

            assertThat(zone.isActive()).isTrue();
        }

        @Test
        void shouldEmitZoneActivatedEvent() {
            zone.pullEvents();

            zone.activate();

            List<Event> events = zone.getEvents();
            assertThat(events).hasSize(1);
            assertThat(events.getFirst()).isInstanceOf(ZoneActivatedEvent.class);
            ZoneActivatedEvent event = (ZoneActivatedEvent) events.getFirst();
            assertThat(event.zoneId()).isEqualTo(ZONE_ID);
            assertThat(event.locationId()).isEqualTo(LOCATION_ID);
        }
    }

    @Nested
    @DisplayName("deactivate")
    class DeactivateTests {

        @Test
        void shouldSetActiveToFalse() {
            zone.deactivate();

            assertThat(zone.isActive()).isFalse();
        }

        @Test
        void shouldBeIdempotent() {
            zone.deactivate();

            zone.deactivate();

            assertThat(zone.isActive()).isFalse();
        }

        @Test
        void shouldEmitZoneDeactivatedEvent() {
            zone.pullEvents();

            zone.deactivate();

            List<Event> events = zone.getEvents();
            assertThat(events).hasSize(1);
            assertThat(events.getFirst()).isInstanceOf(ZoneDeactivatedEvent.class);
            ZoneDeactivatedEvent event = (ZoneDeactivatedEvent) events.getFirst();
            assertThat(event.zoneId()).isEqualTo(ZONE_ID);
            assertThat(event.locationId()).isEqualTo(LOCATION_ID);
        }
    }

    private static Id id(String value) {
        return () -> value;
    }
}
