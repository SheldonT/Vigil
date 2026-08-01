package com.vigil.alarm;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.vigil.exception.InvalidConfigurationException;

class AlarmConfigTest {

    /** Builds a valid alarm config map with sensible defaults. */
    private Map<String, Object> validMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("highAlarm",        90.0);
        map.put("highAlarmClear",   85.0);
        map.put("highWarning",      80.0);
        map.put("highWarningClear", 75.0);
        map.put("lowWarning",       20.0);
        map.put("lowWarningClear",  25.0);
        map.put("lowAlarm",         10.0);
        map.put("lowAlarmClear",    15.0);
        map.put("activationDelayMs", 0L);
        map.put("clearDelayMs",      0L);
        return map;
    }

    @Test
    void fromMap_succeeds_withValidConfig() {
        NumericAlarmConfig config = NumericAlarmConfig.fromMap("CPU", validMap());
        assertNotNull(config);
        assertEquals("CPU", config.getMonitorName());
        assertEquals(90.0, config.getHighAlarm());
        assertEquals(10.0, config.getLowAlarm());
    }

    @Test
    void fromMap_throws_whenHighAlarmLessThanHighWarning() {
        Map<String, Object> map = validMap();
        map.put("highAlarm", 70.0);   // lower than highWarning (80)
        assertThrows(InvalidConfigurationException.class,
                () -> NumericAlarmConfig.fromMap("CPU", map));
    }

    @Test
    void fromMap_throws_whenLowAlarmGreaterThanLowWarning() {
        Map<String, Object> map = validMap();
        map.put("lowAlarm", 30.0);    // higher than lowWarning (20)
        assertThrows(InvalidConfigurationException.class,
                () -> NumericAlarmConfig.fromMap("CPU", map));
    }

    @Test
    void fromMap_throws_whenHighAlarmClearGreaterThanHighAlarm() {
        Map<String, Object> map = validMap();
        map.put("highAlarmClear", 95.0);  // above highAlarm (90)
        assertThrows(InvalidConfigurationException.class,
                () -> NumericAlarmConfig.fromMap("CPU", map));
    }

    @Test
    void fromMap_throws_whenLowAlarmClearLessThanLowAlarm() {
        Map<String, Object> map = validMap();
        map.put("lowAlarmClear", 5.0);    // below lowAlarm (10)
        assertThrows(InvalidConfigurationException.class,
                () -> NumericAlarmConfig.fromMap("CPU", map));
    }

    @Test
    void fromMap_throws_whenLowWarningGreaterThanOrEqualToHighWarning() {
        Map<String, Object> map = validMap();
        map.put("lowWarning", 80.0);  // equal to highWarning
        assertThrows(InvalidConfigurationException.class,
                () -> NumericAlarmConfig.fromMap("CPU", map));
    }

    @Test
    void fromMap_throws_whenRequiredFieldMissing() {
        Map<String, Object> map = validMap();
        map.remove("highAlarm");
        assertThrows(IllegalArgumentException.class,
                () -> NumericAlarmConfig.fromMap("CPU", map));
    }

    @Test
    void fromMap_throws_whenNegativeActivationDelay() {
        Map<String, Object> map = validMap();
        map.put("activationDelayMs", -500L);
        assertThrows(InvalidConfigurationException.class,
                () -> NumericAlarmConfig.fromMap("CPU", map));
    }
}
