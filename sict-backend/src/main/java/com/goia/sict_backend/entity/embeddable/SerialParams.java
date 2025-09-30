package com.goia.sict_backend.entity.embeddable;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class SerialParams {
    private String portName;
    private Integer baudRate;
    private Integer dataBits;
    private String parity;
    private Integer stopBits;
    private String flowControl;
    private Integer readTimeoutMs;
    private Integer writeTimeoutMs;
    private Integer retries;
}
