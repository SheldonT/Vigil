package com.vigil.command;

import com.vigil.message.MessageType;

public interface VigilCommand {
    MessageType type();
    CommandType commandType();
}
