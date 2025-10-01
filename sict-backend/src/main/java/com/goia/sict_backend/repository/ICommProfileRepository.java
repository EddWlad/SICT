package com.goia.sict_backend.repository;

import com.goia.sict_backend.entity.CommProfile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ICommProfileRepository extends IGenericRepository<CommProfile, UUID>{

    List<CommProfile> findByTrafficRegulator_IdTrafficRegulatorAndStatusNot(UUID idTrafficRegulator, Integer status);

    boolean existsByTrafficRegulator_IdTrafficRegulatorAndStatus(UUID idTrafficRegulator, Integer status);
}
