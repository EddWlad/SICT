package com.goia.sict_backend.service.impl;

import com.goia.sict_backend.entity.TrafficRegulator;
import com.goia.sict_backend.repository.IGenericRepository;
import com.goia.sict_backend.repository.ITrafficRegulatorRepository;
import com.goia.sict_backend.service.ITrafficRegulatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrafficRegulatorServiceImpl extends GenericServiceImpl<TrafficRegulator, UUID> implements ITrafficRegulatorService {

    private final ITrafficRegulatorRepository trafficRegulatorRepository;

    @Override
    protected IGenericRepository<TrafficRegulator, UUID> getRepo() {
        return trafficRegulatorRepository;
    }

}
