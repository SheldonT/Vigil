package com.vigil.config;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.vigil.exception.InvalidConfigurationException;

class ConfigValidatorTest {

    // ---- requireMap ----

    @Test
    void requireMap_returnsMap_whenValueIsMap() {
        Map<String, Object> map = Map.of("key", "value");
        Map<String, Object> result = ConfigValidator.requireMap(map, "test");
        assertEquals(map, result);
    }

    @Test
    void requireMap_throws_whenValueIsNull() {
        assertThrows(InvalidConfigurationException.class,
                () -> ConfigValidator.requireMap(null, "test"));
    }

    @Test
    void requireMap_throws_whenValueIsNotMap() {
        assertThrows(InvalidConfigurationException.class,
                () -> ConfigValidator.requireMap("not a map", "test"));
    }

    // ---- requireString ----

    @Test
    void requireString_returnsValue_whenKeyPresent() {
        Map<String, Object> map = Map.of("host", "localhost");
        assertEquals("localhost", ConfigValidator.requireString(map, "id", "host"));
    }

    @Test
    void requireString_throws_whenKeyMissing() {
        Map<String, Object> map = Map.of();
        assertThrows(IllegalArgumentException.class,
                () -> ConfigValidator.requireString(map, "id", "host"));
    }

    @Test
    void requireString_throws_whenValueIsWrongType() {
        Map<String, Object> map = new HashMap<>();
        map.put("host", 42);
        assertThrows(InvalidConfigurationException.class,
                () -> ConfigValidator.requireString(map, "id", "host"));
    }

    // ---- requireDouble ----

    @Test
    void requireDouble_returnsValue_whenKeyPresent() {
        Map<String, Object> map = Map.of("threshold", 75.5);
        assertEquals(75.5, ConfigValidator.requireDouble(map, "id", "threshold"));
    }

    @Test
    void requireDouble_throws_whenKeyMissing() {
        Map<String, Object> map = Map.of();
        assertThrows(IllegalArgumentException.class,
                () -> ConfigValidator.requireDouble(map, "id", "threshold"));
    }

    @Test
    void requireDouble_throws_whenValueIsWrongType() {
        Map<String, Object> map = new HashMap<>();
        map.put("threshold", "not-a-double");
        assertThrows(InvalidConfigurationException.class,
                () -> ConfigValidator.requireDouble(map, "id", "threshold"));
    }

    // ---- requireLong ----

    @Test
    void requireLong_returnsValue_whenKeyPresent() {
        Map<String, Object> map = Map.of("delay", 1000L);
        assertEquals(1000L, ConfigValidator.requireLong(map, "id", "delay"));
    }

    @Test
    void requireLong_throws_whenKeyMissing() {
        Map<String, Object> map = Map.of();
        assertThrows(IllegalArgumentException.class,
                () -> ConfigValidator.requireLong(map, "id", "delay"));
    }

    // ---- requireBool ----

    @Test
    void requireBool_returnsValue_whenKeyPresent() {
        Map<String, Object> map = Map.of("enabled", true);
        assertTrue(ConfigValidator.requireBool(map, "id", "enabled"));
    }

    @Test
    void requireBool_throws_whenKeyMissing() {
        Map<String, Object> map = Map.of();
        assertThrows(IllegalArgumentException.class,
                () -> ConfigValidator.requireBool(map, "id", "enabled"));
    }
}
