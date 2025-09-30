package com.goia.sict_backend.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.goia.sict_backend.dto.embeddableDTO.BluetoothParamsDTO;
import com.goia.sict_backend.dto.embeddableDTO.EthernetParamsDTO;
import com.goia.sict_backend.dto.embeddableDTO.SerialParamsDTO;
import com.goia.sict_backend.dto.embeddableDTO.UsbParamsDTO;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;

import org.modelmapper.Converter;
import org.modelmapper.TypeMap;

import com.goia.sict_backend.dto.*;
import com.goia.sict_backend.entity.*;
import com.goia.sict_backend.entity.embeddable.*;
import com.goia.sict_backend.entity.enums.InterfaceType;

@Configuration
public class MapperConfig {

    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }

    @Bean(name = "defaultMapper")
    public ModelMapper defaultMapper() {
        return new ModelMapper();
    }

    /*@Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }*/

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        // ===== TrafficRegulator =====
        mapper.createTypeMap(TrafficRegulator.class, TrafficRegulatorDTO.class);
        mapper.createTypeMap(TrafficRegulatorDTO.class, TrafficRegulator.class);

        // ===== Converters enum <-> String =====
        Converter<InterfaceType, String> enumToString =
                ctx -> ctx.getSource() == null ? null : ctx.getSource().name();

        Converter<String, InterfaceType> stringToEnum =
                ctx -> ctx.getSource() == null ? null : InterfaceType.valueOf(ctx.getSource());

        // ===== CommProfile -> CommProfileDTO =====
        TypeMap<CommProfile, CommProfileDTO> cpToDto = mapper.createTypeMap(CommProfile.class, CommProfileDTO.class);
        cpToDto.addMappings(m -> {
            m.using(enumToString).map(CommProfile::getInterfaceType, CommProfileDTO::setInterfaceType);
            // Relación → UUID
            m.map(src -> src.getTrafficRegulator().getIdTrafficRegulator(), CommProfileDTO::setIdTrafficRegulator);
        });

        // Embeddables -> sub-DTOs (nombres con prefijo)
        mapper.createTypeMap(SerialParams.class, SerialParamsDTO.class)
                .addMappings(m -> {
                    m.map(SerialParams::getPortName, SerialParamsDTO::setSerialPortName);
                    m.map(SerialParams::getBaudRate, SerialParamsDTO::setSerialBaudRate);
                    m.map(SerialParams::getDataBits, SerialParamsDTO::setSerialDataBits);
                    m.map(SerialParams::getParity,   SerialParamsDTO::setSerialParity);
                    m.map(SerialParams::getStopBits, SerialParamsDTO::setSerialStopBits);
                    m.map(SerialParams::getFlowControl, SerialParamsDTO::setSerialFlowControl);
                    m.map(SerialParams::getReadTimeoutMs, SerialParamsDTO::setSerialReadTimeoutMs);
                    m.map(SerialParams::getWriteTimeoutMs, SerialParamsDTO::setSerialWriteTimeoutMs);
                    m.map(SerialParams::getRetries, SerialParamsDTO::setSerialRetries);
                });

        mapper.createTypeMap(EthernetParams.class, EthernetParamsDTO.class)
                .addMappings(m -> {
                    m.map(EthernetParams::getHost, EthernetParamsDTO::setEthernetHost);
                    m.map(EthernetParams::getPort, EthernetParamsDTO::setEthernetPort);
                    m.map(src -> src.getProtocol() == null ? null : src.getProtocol().name(),
                            EthernetParamsDTO::setEthernetProtocol);
                    m.map(EthernetParams::getUseTls, EthernetParamsDTO::setEthernetUseTls);
                    m.map(EthernetParams::getReadTimeoutMs, EthernetParamsDTO::setEthernetReadTimeoutMs);
                    m.map(EthernetParams::getWriteTimeoutMs, EthernetParamsDTO::setEthernetWriteTimeoutMs);
                    m.map(EthernetParams::getRetries, EthernetParamsDTO::setEthernetRetries);
                });

        mapper.createTypeMap(UsbParams.class, UsbParamsDTO.class)
                .addMappings(m -> {
                    m.map(src -> src.getMode() == null ? null : src.getMode().name(), UsbParamsDTO::setUsbMode);
                    m.map(UsbParams::getDevicePath, UsbParamsDTO::setUsbDevicePath);
                    m.map(UsbParams::getVid, UsbParamsDTO::setUsbVid);
                    m.map(UsbParams::getPid, UsbParamsDTO::setUsbPid);
                    m.map(UsbParams::getReadTimeoutMs, UsbParamsDTO::setUsbReadTimeoutMs);
                    m.map(UsbParams::getWriteTimeoutMs, UsbParamsDTO::setUsbWriteTimeoutMs);
                    m.map(UsbParams::getRetries, UsbParamsDTO::setUsbRetries);
                });

        mapper.createTypeMap(BluetoothParams.class, BluetoothParamsDTO.class)
                .addMappings(m -> {
                    m.map(src -> src.getProfile() == null ? null : src.getProfile().name(), BluetoothParamsDTO::setBtProfile);
                    m.map(BluetoothParams::getAddress, BluetoothParamsDTO::setBtAddress);
                    m.map(BluetoothParams::getPairingKey, BluetoothParamsDTO::setBtPairingKey);
                    m.map(BluetoothParams::getReadTimeoutMs, BluetoothParamsDTO::setBtReadTimeoutMs);
                    m.map(BluetoothParams::getWriteTimeoutMs, BluetoothParamsDTO::setBtWriteTimeoutMs);
                    m.map(BluetoothParams::getRetries, BluetoothParamsDTO::setBtRetries);
                });

        // ===== CommProfileDTO -> CommProfile =====
        TypeMap<CommProfileDTO, CommProfile> dtoToCp = mapper.createTypeMap(CommProfileDTO.class, CommProfile.class);
        dtoToCp.addMappings(m -> {
            m.using(stringToEnum).map(CommProfileDTO::getInterfaceType, CommProfile::setInterfaceType);
            // OJO: trafficRegulator lo setea el Service (buscando por id), no el mapper.
        });

        // Sub-DTOs -> Embeddables
        mapper.createTypeMap(SerialParamsDTO.class, SerialParams.class)
                .addMappings(m -> {
                    m.map(SerialParamsDTO::getSerialPortName, SerialParams::setPortName);
                    m.map(SerialParamsDTO::getSerialBaudRate, SerialParams::setBaudRate);
                    m.map(SerialParamsDTO::getSerialDataBits, SerialParams::setDataBits);
                    m.map(SerialParamsDTO::getSerialParity,   SerialParams::setParity);
                    m.map(SerialParamsDTO::getSerialStopBits, SerialParams::setStopBits);
                    m.map(SerialParamsDTO::getSerialFlowControl, SerialParams::setFlowControl);
                    m.map(SerialParamsDTO::getSerialReadTimeoutMs, SerialParams::setReadTimeoutMs);
                    m.map(SerialParamsDTO::getSerialWriteTimeoutMs, SerialParams::setWriteTimeoutMs);
                    m.map(SerialParamsDTO::getSerialRetries, SerialParams::setRetries);
                });

        mapper.createTypeMap(EthernetParamsDTO.class, EthernetParams.class)
                .addMappings(m -> {
                    m.map(EthernetParamsDTO::getEthernetHost, EthernetParams::setHost);
                    m.map(EthernetParamsDTO::getEthernetPort, EthernetParams::setPort);
                    m.map(src -> src.getEthernetProtocol() == null ? null : com.goia.sict_backend.entity.enums.IpProtocol.valueOf(src.getEthernetProtocol()),
                            EthernetParams::setProtocol);
                    m.map(EthernetParamsDTO::getEthernetUseTls, EthernetParams::setUseTls);
                    m.map(EthernetParamsDTO::getEthernetReadTimeoutMs, EthernetParams::setReadTimeoutMs);
                    m.map(EthernetParamsDTO::getEthernetWriteTimeoutMs, EthernetParams::setWriteTimeoutMs);
                    m.map(EthernetParamsDTO::getEthernetRetries, EthernetParams::setRetries);
                });

        mapper.createTypeMap(UsbParamsDTO.class, UsbParams.class)
                .addMappings(m -> {
                    m.map(src -> src.getUsbMode() == null ? null : com.goia.sict_backend.entity.enums.UsbMode.valueOf(src.getUsbMode()),
                            UsbParams::setMode);
                    m.map(UsbParamsDTO::getUsbDevicePath, UsbParams::setDevicePath);
                    m.map(UsbParamsDTO::getUsbVid, UsbParams::setVid);
                    m.map(UsbParamsDTO::getUsbPid, UsbParams::setPid);
                    m.map(UsbParamsDTO::getUsbReadTimeoutMs, UsbParams::setReadTimeoutMs);
                    m.map(UsbParamsDTO::getUsbWriteTimeoutMs, UsbParams::setWriteTimeoutMs);
                    m.map(UsbParamsDTO::getUsbRetries, UsbParams::setRetries);
                });

        mapper.createTypeMap(BluetoothParamsDTO.class, BluetoothParams.class)
                .addMappings(m -> {
                    m.map(src -> src.getBtProfile() == null ? null : com.goia.sict_backend.entity.enums.BluetoothProfile.valueOf(src.getBtProfile()),
                            BluetoothParams::setProfile);
                    m.map(BluetoothParamsDTO::getBtAddress, BluetoothParams::setAddress);
                    m.map(BluetoothParamsDTO::getBtPairingKey, BluetoothParams::setPairingKey);
                    m.map(BluetoothParamsDTO::getBtReadTimeoutMs, BluetoothParams::setReadTimeoutMs);
                    m.map(BluetoothParamsDTO::getBtWriteTimeoutMs, BluetoothParams::setWriteTimeoutMs);
                    m.map(BluetoothParamsDTO::getBtRetries, BluetoothParams::setRetries);
                });

        return mapper;
    }
}
