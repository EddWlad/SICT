package com.goia.sict_backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.goia.sict_backend.entity.embeddable.BluetoothParams;
import com.goia.sict_backend.entity.embeddable.EthernetParams;
import com.goia.sict_backend.entity.embeddable.SerialParams;
import com.goia.sict_backend.entity.embeddable.UsbParams;
import com.goia.sict_backend.entity.enums.InterfaceType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "comm_profile")
public class CommProfile {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false, columnDefinition = "uuid")
    @EqualsAndHashCode.Include
    private UUID idCommProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "id_traffic_regulator",
            nullable = false,
            columnDefinition = "uuid",
            foreignKey = @ForeignKey(name = "FK_COMM_PROFILE_TRAFFIC_REGULATOR")
    )
    @ToString.Exclude
    private TrafficRegulator trafficRegulator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private InterfaceType interfaceType;

    // -------- RS232 --------
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "portName",       column = @Column(name = "serial_port_name")),
            @AttributeOverride(name = "baudRate",       column = @Column(name = "serial_baud_rate")),
            @AttributeOverride(name = "dataBits",       column = @Column(name = "serial_data_bits")),
            @AttributeOverride(name = "parity",         column = @Column(name = "serial_parity")),
            @AttributeOverride(name = "stopBits",       column = @Column(name = "serial_stop_bits")),
            @AttributeOverride(name = "flowControl",    column = @Column(name = "serial_flow_control")),
            @AttributeOverride(name = "readTimeoutMs",  column = @Column(name = "serial_read_timeout_ms")),
            @AttributeOverride(name = "writeTimeoutMs", column = @Column(name = "serial_write_timeout_ms")),
            @AttributeOverride(name = "retries",        column = @Column(name = "serial_retries"))
    })
    private SerialParams serialParams;

    // -------- Ethernet --------
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "host",           column = @Column(name = "ethernet_host")),
            @AttributeOverride(name = "port",           column = @Column(name = "ethernet_port")),
            @AttributeOverride(name = "protocol",       column = @Column(name = "ethernet_protocol")),
            @AttributeOverride(name = "useTls",         column = @Column(name = "ethernet_use_tls")),
            @AttributeOverride(name = "readTimeoutMs",  column = @Column(name = "ethernet_read_timeout_ms")),
            @AttributeOverride(name = "writeTimeoutMs", column = @Column(name = "ethernet_write_timeout_ms")),
            @AttributeOverride(name = "retries",        column = @Column(name = "ethernet_retries"))
    })
    private EthernetParams ethernetParams;

    // -------- USB --------
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "mode",           column = @Column(name = "usb_mode")),
            @AttributeOverride(name = "devicePath",     column = @Column(name = "usb_device_path")),
            @AttributeOverride(name = "vid",            column = @Column(name = "usb_vid")),
            @AttributeOverride(name = "pid",            column = @Column(name = "usb_pid")),
            @AttributeOverride(name = "readTimeoutMs",  column = @Column(name = "usb_read_timeout_ms")),
            @AttributeOverride(name = "writeTimeoutMs", column = @Column(name = "usb_write_timeout_ms")),
            @AttributeOverride(name = "retries",        column = @Column(name = "usb_retries"))
    })
    private UsbParams usbParams;

    // -------- Bluetooth --------
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "profile",        column = @Column(name = "bt_profile")),
            @AttributeOverride(name = "address",        column = @Column(name = "bt_address")),
            @AttributeOverride(name = "pairingKey",     column = @Column(name = "bt_pairing_key")),
            @AttributeOverride(name = "readTimeoutMs",  column = @Column(name = "bt_read_timeout_ms")),
            @AttributeOverride(name = "writeTimeoutMs", column = @Column(name = "bt_write_timeout_ms")),
            @AttributeOverride(name = "retries",        column = @Column(name = "bt_retries"))
    })
    private BluetoothParams bluetoothParams;

    private Integer commProfileKeepAliveSec;
    private Integer commProfileOverallTimeoutMs;

    @CreationTimestamp
    @Column(name = "comm_profile_date_create", updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime commProfileDateCreate;

    @UpdateTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime commProfileDateUpdate;

    @Column(nullable = false)
    private Integer status = 1;
}
