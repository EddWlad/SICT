package com.goia.sict_backend.service;

import com.goia.sict_backend.comm.MCommandType;
import com.goia.sict_backend.dto.CommFrameRequestDTO;
import com.goia.sict_backend.dto.CommTestRequestDTO;
import com.goia.sict_backend.dto.CommTestResultDTO;
import com.goia.sict_backend.dto.ProtocolMCommandDTO;

import java.util.Map;
import java.util.UUID;

public interface ICommTestService {
    CommTestResultDTO testConnection(UUID idCommProfile) throws Exception;
    // NUEVO: prueba de envío/lectura de trama
    CommTestResultDTO testFrame1(UUID idCommProfile, CommTestRequestDTO req) throws Exception;

    CommTestResultDTO testFrame(UUID idCommProfile, CommFrameRequestDTO req) throws Exception;

    CommTestResultDTO sendMCommand(UUID idCommProfile, ProtocolMCommandDTO cmd) throws Exception;

    CommTestResultDTO sendMCommandType(UUID idCommProfile, MCommandType type, Map<String,Object> args) throws Exception;

}
