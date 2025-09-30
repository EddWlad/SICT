package com.goia.sict_backend.repository;

import com.goia.sict_backend.entity.CommProfile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ICommProfileRepository extends IGenericRepository<CommProfile, UUID>{

    //List<CommProfile> findByTrafficRegulator_IdTrafficRegulatorAndCommProfileStatusNot(UUID idTrafficRegulator, Integer status);

    //Optional<CommProfile> findFirstByTrafficRegulator_IdTrafficRegulatorAndCommProfileStatusNotOrderByCommProfileDateUpdateDesc(UUID idTrafficRegulator, Integer status);

    //boolean existsByTrafficRegulator_IdTrafficRegulatorAndCommProfileStatus(UUID idTrafficRegulator, Integer status);
}
