package de.intelligence.drp.api.exception;

import java.util.Arrays;

public enum ErrorCode {

    UNSPECIFIED(-1),
    PIPE_RESERVATION_FAILED(0x01),
    ALREADY_INITIALIZED(0x02),
    PIPE_NOT_FOUND(0x03),
    PIPE_BUSY(0x04),
    ACCESS_DENIED(0x05),
    BYTE_MISMATCH(0x06),
    PIPE_ENDED(0x07);

    private final int errorCode;

    ErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public static ErrorCode getFromInt(int errorCode) {
        return Arrays.stream(ErrorCode.values()).filter(e -> e.errorCode == errorCode).findAny().orElse(ErrorCode.UNSPECIFIED);
    }

}
