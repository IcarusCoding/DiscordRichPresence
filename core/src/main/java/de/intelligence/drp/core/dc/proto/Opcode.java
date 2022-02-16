package de.intelligence.drp.core.dc.proto;

import java.util.Arrays;

public enum Opcode {

    HANDSHAKE(0),
    FRAME(1);

    private final int opcode;

    Opcode(int opcode) {
        this.opcode = opcode;
    }

    public int getOpcode() {
        return this.opcode;
    }

    public static Opcode fromCode(int code) {
        return Arrays.stream(Opcode.values()).filter(o -> o.opcode == code).findFirst().orElse(null);
    }

}
