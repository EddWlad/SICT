package com.goia.sict_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TrafficRegulatorDTO {
    private UUID idTrafficRegulator;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime trafficRegulatorDateCreate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime trafficRegulatorDateUpdate;

    private String trafficRegulatorName;
    private String trafficRegulatorManufacturer;
    private String trafficRegulatorModel;
    private String trafficRegulatorSerialNumber;
    private String trafficRegulatorLocation;

    private Integer status;
}
