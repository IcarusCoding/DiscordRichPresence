package de.intelligence.drp.dc.proto;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public record Frame(Opcode opcode, int length, byte[] payload) {

    public byte[] toByteArray() {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final byte[] opcode = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putInt(this.opcode.getOpcode()).array();
        for(final byte b : opcode) {
            baos.write(b);
        }
        final byte[] length = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putInt(this.length).array();
        for(final byte b : length) {
            baos.write(b);
        }
        for (int i = 0; i < this.length; i++) {
            baos.write(this.payload[i]);
        }
        return baos.toByteArray();
    }

}
