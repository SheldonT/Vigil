package com.vigil.command;

import com.fasterxml.jackson.databind.JsonNode;

public interface VigilCommand {
    CommandType commandType();
    JsonNode payload();
}
