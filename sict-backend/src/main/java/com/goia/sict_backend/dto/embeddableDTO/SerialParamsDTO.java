package com.goia.sict_backend.dto.embeddableDTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SerialParamsDTO {
    private String serialPortName;
    private Integer serialBaudRate;
    private Integer serialDataBits;
    private String serialParity;
    private Integer serialStopBits;
    private String serialFlowControl;
    private Integer serialReadTimeoutMs;
    private Integer serialWriteTimeoutMs;
    private Integer serialRetries;
}
