package de.intelligence.drp.dc.proto;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;

public final class Frame {

    private Opcode opcode;
    private int length;
    private byte[] payload;

    public Frame(Opcode opcode, int length, byte[] payload) {
        this.opcode = opcode;
        this.length = length;
        this.payload = payload;
    }

    public byte[] toByteArray() {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final byte[] opcode = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putInt(this.opcode.getOpcode()).array();
        for (final byte b : opcode) {
            baos.write(b);
        }
        final byte[] length = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putInt(this.length).array();
        for (final byte b : length) {
            baos.write(b);
        }
        for (int i = 0; i < this.length; i++) {
            baos.write(this.payload[i]);
        }
        return baos.toByteArray();
    }

    public static Frame fromByteArray(byte[] buf) {
        int opcode = -1;
        int length = 0;
        byte[] payload = new byte[0];
        if (buf.length > 3) {
            opcode = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).put(buf, 0, 4).flip().getInt();
            System.out.println(opcode);
        }
        if (buf.length > 7) {
            length = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).put(buf, 4, 4).flip().getInt();
        }
        if (buf.length > 8) {
            payload = new byte[buf.length - 8];
            System.arraycopy(buf, 9, payload, 0, payload.length); // 8 or 9?
        }
        return new Frame(Opcode.fromCode(opcode), length, payload);
    }

    public Opcode getOpcode() {
        return this.opcode;
    }

    public int getLength() {
        return this.length;
    }

    public int getFullLength() {
        return this.length + 8; // with header
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public void setOpcode(Opcode opcode) {
        this.opcode = opcode;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Frame) obj;
        return Objects.equals(this.opcode, that.opcode) &&
                this.length == that.length &&
                Arrays.equals(this.payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opcode, length, Arrays.hashCode(payload));
    }

    @Override
    public String toString() {
        return "Frame[" +
                "opcode=" + opcode + ", " +
                "length=" + length + ", " +
                "payload=" + Arrays.toString(payload) + ']';
    }

}
