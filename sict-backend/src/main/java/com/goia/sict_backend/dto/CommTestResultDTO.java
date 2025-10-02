package com.goia.sict_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommTestResultDTO {
    private boolean ok;
    private String interfaceType;
    private long elapsedMs;
    private String message;
    private String details;
}
