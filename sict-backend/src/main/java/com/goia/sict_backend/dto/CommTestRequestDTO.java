package com.goia.sict_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommTestRequestDTO {
    private String asciiPayload;
    private String hexPayload;

    private Integer bytesToRead;
    private Integer readTimeoutMsOverride;
    private String expectAsciiContains;
    private Integer delayBeforeReadMs;
}
