package com.goia.sict_backend.service.impl;


import com.goia.sict_backend.entity.Role;
import com.goia.sict_backend.entity.User;
import com.goia.sict_backend.repository.IGenericRepository;
import com.goia.sict_backend.repository.IUserRepository;
import com.goia.sict_backend.repository.IUserRoleRepository;
import com.goia.sict_backend.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends GenericServiceImpl<User, UUID> implements IUserService {

    private final IUserRepository userRepository;
    private final IUserRoleRepository urRepo;
    private final PasswordEncoder passwordEncoder;


    @Override
    protected IGenericRepository<User, UUID> getRepo() {
        return userRepository;
    }

    @Override
    public User findOneByEmail(String email) {
        return userRepository.findOneByEmail(email);
    }

    @Override
    public void changePassword(String username, String newPassword) {
        //userRepository.changePassword(username, bcrypt.encode(newPassword)) ;
    }

    @Transactional
    @Override
    public User saveTransactional(User user, List<Role> roles) throws Exception {
        User optionalUser = userRepository.findOneByEmail(user.getEmail());
        if(optionalUser != null) {
            User existingUser = optionalUser;
            if (existingUser.getStatus() == 0) {
                // Reactivate and update user data
                existingUser.setStatus(1);
                existingUser.setFullName(user.getFullName());
                existingUser.setEmail(user.getEmail());
                existingUser.setDateCreate(LocalDateTime.now());
                if (user.getPassword() != null && !user.getPassword().isBlank()) {
                    if (user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$") || user.getPassword().startsWith("$2y$")) {
                        existingUser.setPassword(user.getPassword());
                    } else {
                        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
                    }
                }
                userRepository.save(existingUser);

                urRepo.deleteByUserId(existingUser.getIdUser());
                roles.forEach(role -> urRepo.saveRole(existingUser.getIdUser(), role.getIdRole()));

                return existingUser;
            } else {
                throw new RuntimeException("A user with this identification already exists");
            }
        }
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            if (user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$") || user.getPassword().startsWith("$2y$")) {
            } else {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        }
        userRepository.save(user);
        roles.forEach(role -> urRepo.saveRole(user.getIdUser(), role.getIdRole()));

        return user;

    }

    @Transactional
    @Override
    public User updateTransactional(UUID id, User user, List<Role> roles) throws Exception {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ID NOT FOUND " + id));

        if (existingUser.getStatus() == 0) {
            throw new RuntimeException("The user was logically deleted. Cannot update.");
        }

        existingUser.setFullName(user.getFullName());
        existingUser.setEmail(user.getEmail());
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            if (user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$") || user.getPassword().startsWith("$2y$")) {
                existingUser.setPassword(user.getPassword());
            } else {
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        }
        existingUser.setStatus(user.getStatus());

        userRepository.save(existingUser);

        urRepo.softDeleteByUserId(id);
        roles.forEach(role -> urRepo.saveRole(id, role.getIdRole()));

        return existingUser;
    }

    @Override
    public User findUserDetail(UUID id) throws Exception {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public List<User> findActiveUsersByRole(String roleName) throws Exception {
        return userRepository.findUsersByRoleNameAndStatus(roleName);
    }

    @Transactional
    @Override
    public boolean softDeleteTransactional(UUID id) throws Exception {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ID NOT FOUND " + id));

        user.setStatus(0);
        userRepository.save(user);

        urRepo.softDeleteByUserId(id);

        return true;
    }
}