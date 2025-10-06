package com.goia.sict_backend.dto;

import java.util.List;

public record CommFrameRequestDTO (
        List<Integer> decimals,

        Integer expectBytes,
        Integer readTimeoutMs,

        List<Integer> expectPrefix,
        List<Integer> expectContains
) {
}
