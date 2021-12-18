package de.intelligence.drp.dc.proto;

public enum Opcode {

    HANDSHAKE(0);

    private final int opcode;

    Opcode(int opcode) {
        this.opcode = opcode;
    }

    public int getOpcode() {
        return this.opcode;
    }

}
