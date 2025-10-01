package com.goia.sict_backend.service.impl;

import com.goia.sict_backend.entity.Role;
import com.goia.sict_backend.repository.IGenericRepository;
import com.goia.sict_backend.repository.IRoleRepository;
import com.goia.sict_backend.service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends GenericServiceImpl<Role, UUID> implements IRoleService {

    private final IRoleRepository roleRepository;

    @Override
    protected IGenericRepository<Role, UUID> getRepo() {
        return roleRepository;
    }

    @Override
    public Page<Role> listPage(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }


}
