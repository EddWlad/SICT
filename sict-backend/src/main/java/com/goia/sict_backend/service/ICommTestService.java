package com.goia.sict_backend.service;

import com.goia.sict_backend.dto.CommTestRequestDTO;
import com.goia.sict_backend.dto.CommTestResultDTO;

import java.util.UUID;

public interface ICommTestService {
    CommTestResultDTO testConnection(UUID idCommProfile) throws Exception;
    // NUEVO: prueba de envío/lectura de trama
    CommTestResultDTO testFrame(UUID idCommProfile, CommTestRequestDTO req) throws Exception;

}
