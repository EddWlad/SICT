package com.goia.sict_backend.service.impl;

import com.goia.sict_backend.entity.CommProfile;
import com.goia.sict_backend.repository.ICommProfileRepository;
import com.goia.sict_backend.repository.IGenericRepository;
import com.goia.sict_backend.repository.ITrafficRegulatorRepository;
import com.goia.sict_backend.service.ICommProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommProfileServiceImpl extends GenericServiceImpl<CommProfile, UUID> implements ICommProfileService {

    private final ICommProfileRepository commProfileRepository;


    @Override
    protected IGenericRepository<CommProfile, UUID> getRepo() {
        return commProfileRepository;
    }

    @Override
    public List<CommProfile> findByRegulator(UUID idTrafficRegulator) throws Exception{

        return commProfileRepository
                .findByTrafficRegulator_IdTrafficRegulatorAndStatusNot(idTrafficRegulator, 0);
    }

    @Override
    public boolean existsActiveForRegulator(UUID idTrafficRegulator) throws Exception{
        return commProfileRepository
                .existsByTrafficRegulator_IdTrafficRegulatorAndStatus(idTrafficRegulator, 1);
    }
}
