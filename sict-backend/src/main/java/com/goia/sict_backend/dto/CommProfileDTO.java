package com.goia.sict_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.goia.sict_backend.dto.embeddableDTO.BluetoothParamsDTO;
import com.goia.sict_backend.dto.embeddableDTO.EthernetParamsDTO;
import com.goia.sict_backend.dto.embeddableDTO.SerialParamsDTO;
import com.goia.sict_backend.dto.embeddableDTO.UsbParamsDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CommProfileDTO {
    private UUID idCommProfile;

    private UUID idTrafficRegulator;

    private String interfaceType;

    private SerialParamsDTO serialParams;
    private EthernetParamsDTO ethernetParams;
    private UsbParamsDTO usbParams;
    private BluetoothParamsDTO bluetoothParams;

    private Integer commProfileKeepAliveSec;
    private Integer commProfileOverallTimeoutMs;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime commProfileDateCreate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime commProfileDateUpdate;

    private Integer status;
}
