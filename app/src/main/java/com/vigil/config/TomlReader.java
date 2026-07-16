package com.vigil.config;

import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.tomlj.Toml;
import org.tomlj.TomlTable;
import org.tomlj.TomlParseResult;

public class TomlReader {

    private final TomlParseResult config;

    public TomlReader (String file) throws IOException {

        InputStream configStream = new java.io.FileInputStream(file);
        this.config = Toml.parse(configStream);
    }

    private static final Logger logger = Logger.getLogger(TomlReader.class.getName());

    public Map<String, Object> getRoot(){

        logger.info("Parsing root TOML table to map...");
        Map<String, Object> standardTable = new HashMap<>();

        Map<String, Object> root = this.config.toMap();

        for (Map.Entry<String, Object> entry : root.entrySet()){
            if (entry.getValue() instanceof TomlTable subTable) {
                standardTable.put(entry.getKey(), deepConvert(subTable));
            } else {
                standardTable.put(entry.getKey(), entry.getValue());
            }
        }
        logger.info("Done.");
        return standardTable;
    }

    public Map<String, Object> getTable(String path) {

        logger.info("Parsing TOML table " + path + " to map...");

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
        logger.info("Done.");
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
