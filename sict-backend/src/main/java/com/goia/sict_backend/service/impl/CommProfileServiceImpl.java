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
    private final ITrafficRegulatorRepository regulatorRepository;


    @Override
    protected IGenericRepository<CommProfile, UUID> getRepo() {
        return commProfileRepository;
    }
}
