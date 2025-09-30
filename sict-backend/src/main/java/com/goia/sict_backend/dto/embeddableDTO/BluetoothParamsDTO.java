package com.goia.sict_backend.dto.embeddableDTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BluetoothParamsDTO {
    private String btProfile;
    private String btAddress;
    private String btPairingKey;
    private Integer btReadTimeoutMs;
    private Integer btWriteTimeoutMs;
    private Integer btRetries;
}
