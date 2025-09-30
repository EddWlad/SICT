package com.goia.sict_backend.entity.embeddable;

import com.goia.sict_backend.entity.enums.UsbMode;
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
public class UsbParams {
    @Enumerated(EnumType.STRING)
    private UsbMode mode;
    private String devicePath;
    private String vid;
    private String pid;
    private Integer readTimeoutMs;
    private Integer writeTimeoutMs;
    private Integer retries;
}
