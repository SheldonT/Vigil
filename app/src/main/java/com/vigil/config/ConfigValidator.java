package com.vigil.config;

import java.util.Map;
import com.vigil.exception.InvalidConfigurationException;

public final class ConfigValidator {
    private ConfigValidator() {}

    @SuppressWarnings("unchecked")
    public static Map<String, Object> requireMap(
            Object value,
            String description) {
        try {
            if (!(value instanceof Map<?, ?> map)) {
                throw new InvalidConfigurationException(
                    description + " must be a table."
                );
            }

            return (Map<String, Object>) map;
        } catch(ClassCastException e){
            throw new InvalidConfigurationException("Invalid map in configuration");
        }
    }

    public static String requireString(Map<String, Object> table, String id, String key) {
        try {
            String value = (String)table.get(key);

            if (value == null) {
                throw new IllegalArgumentException("Missing required string field '" + key + "' in configuration for '" + id + "'");
            }
            return value;
        } catch(ClassCastException e){
            throw new InvalidConfigurationException("Invalid type for String field '" + key + "' in configuration for'" + id + "'");
        }
    }

    public static double requireDouble(Map<String, Object> table, String id, String key){
        try{
            Double value = (Double)table.get(key);
            if (value == null) {
                throw new IllegalArgumentException("Missing required double field '" + key + "' in configuration for'" + id + "'");
            }
            return value;
        } catch(ClassCastException e){
            throw new InvalidConfigurationException("Invalid type for double field '" + key + "' in configuration for'" + id + "'");
        }
    }

    public static long requireLong(Map<String, Object> table, String id, String key) {
        try{
            Long value = (Long)table.get(key);
            if (value == null) {
                throw new IllegalArgumentException("Missing required long field '" + key + "' in configuration for '" + id + "'");
            }
            return value;
        } catch(ClassCastException e){
            throw new InvalidConfigurationException("Invalid type for long field '" + key + "' in configuration for'" + id + "'");
        }
    }

    public static boolean requireBool(Map<String, Object> table, String id, String key) {
        try{
            Boolean value = (Boolean)table.get(key);
            if (value == null) {
                throw new IllegalArgumentException("Missing required boolean field '" + key + "' in configuration for '" + id + "'");
            }
            return value;
        } catch(ClassCastException e){
            throw new InvalidConfigurationException("Invalid type for boolean field '" + key + "' in configuration for'" + id + "'");
        }
    }
}
