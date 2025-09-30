package com.goia.sict_backend.entity.embeddable;

import com.goia.sict_backend.entity.enums.IpProtocol;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class EthernetParams {
    private String host;
    private Integer port;
    @Enumerated(EnumType.STRING)
    private IpProtocol protocol;
    private Boolean useTls;
    private Integer readTimeoutMs;
    private Integer writeTimeoutMs;
    private Integer retries;
}
