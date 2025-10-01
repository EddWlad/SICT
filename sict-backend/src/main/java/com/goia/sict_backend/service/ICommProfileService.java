package com.goia.sict_backend.service;

import com.goia.sict_backend.entity.CommProfile;

import java.util.List;
import java.util.UUID;

public interface ICommProfileService extends IGenericService<CommProfile, UUID>{
    List<CommProfile> findByRegulator(UUID idTrafficRegulator) throws Exception;

    boolean existsActiveForRegulator(UUID idTrafficRegulator) throws Exception;
}
