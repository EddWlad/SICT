package com.goia.sict_backend.entity.embeddable;

import com.goia.sict_backend.entity.enums.BluetoothProfile;
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
public class BluetoothParams {
    @Enumerated(EnumType.STRING)
    private BluetoothProfile profile;
    private String address;
    private String pairingKey;
    private Integer readTimeoutMs;
    private Integer writeTimeoutMs;
    private Integer retries;
}
