package com.goia.sict_backend.repository;

import com.goia.sict_backend.entity.TrafficRegulator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ITrafficRegulatorRepository extends IGenericRepository<TrafficRegulator, UUID>{

    //List<TrafficRegulator> findByTrafficRegulatorStatusNot(Integer status);

    //List<TrafficRegulator> findByTrafficRegulatorStatusNotAndTrafficRegulatorNameContainingIgnoreCase(Integer status, String name);

    Optional<TrafficRegulator> findByTrafficRegulatorName(String name);
}
