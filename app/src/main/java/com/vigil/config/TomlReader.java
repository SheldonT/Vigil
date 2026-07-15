package com.vigil.config;

import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.tomlj.Toml;
import org.tomlj.TomlTable;
import org.tomlj.TomlParseResult;

public class TomlReader {

    private final TomlParseResult config;

    public TomlReader (String file) throws IOException {

        InputStream configStream = new java.io.FileInputStream(file);
        this.config = Toml.parse(configStream);
    }

    public Map<String, Object> getRoot(){
        Map<String, Object> standardTable = new HashMap<>();

        Map<String, Object> root = this.config.toMap();

        for (Map.Entry<String, Object> entry : root.entrySet()){
            if (entry.getValue() instanceof TomlTable subTable) {
                standardTable.put(entry.getKey(), deepConvert(subTable));
            } else {
                standardTable.put(entry.getKey(), entry.getValue());
            }
        }

        return standardTable;
    }

    public Map<String, Object> getTable(String path) {

        Map<String, Object> standardTable = new HashMap<>();

        TomlTable table = this.config.getTable(path);

        if (table == null) return null;

        for (Map.Entry<String, Object> entry : table.entrySet()){
            if (entry.getValue() instanceof TomlTable subTable) {
                standardTable.put(entry.getKey(), deepConvert(subTable));
            } else {
                standardTable.put(entry.getKey(), entry.getValue());
            }
        }
        return standardTable;
    }

    private static Map<String, Object> deepConvert(TomlTable table) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : table.entrySet()) {
            if (entry.getValue() instanceof TomlTable subTable) {
                result.put(entry.getKey(), deepConvert(subTable));
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
