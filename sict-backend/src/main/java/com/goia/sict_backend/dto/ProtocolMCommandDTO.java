package com.goia.sict_backend.dto;

import java.util.List;

public record ProtocolMCommandDTO (
        List<Integer> payloadDecimals,

        boolean wrapWithStxEtx,

        ChecksumMode checksumMode,

        Boolean expectAck,

        Integer expectBytes,      // 0 = leer hasta timeout
        Integer readTimeoutMs     // override opcional
) {
    public enum ChecksumMode
    {   NONE,
        XOR7,
        SUM8
    }

}
