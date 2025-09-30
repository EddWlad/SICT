package com.goia.sict_backend.dto.embeddableDTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UsbParamsDTO {
    private String usbMode;
    private String usbDevicePath;
    private String usbVid;
    private String usbPid;
    private Integer usbReadTimeoutMs;
    private Integer usbWriteTimeoutMs;
    private Integer usbRetries;
}
