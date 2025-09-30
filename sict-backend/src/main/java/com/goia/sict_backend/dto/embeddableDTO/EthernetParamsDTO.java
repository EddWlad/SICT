package com.goia.sict_backend.dto.embeddableDTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EthernetParamsDTO {
    private String ethernetHost;
    private Integer ethernetPort;
    private String ethernetProtocol;
    private Boolean ethernetUseTls;
    private Integer ethernetReadTimeoutMs;
    private Integer ethernetWriteTimeoutMs;
    private Integer ethernetRetries;
}
