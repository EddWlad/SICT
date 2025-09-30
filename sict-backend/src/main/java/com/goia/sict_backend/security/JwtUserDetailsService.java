package com.goia.sict_backend.security;

import com.goia.sict_backend.repository.IUserRepository;
import com.goia.sict_backend.repository.IUserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {

    private final IUserRepository userRepo;
    private final IUserRoleRepository userRoleRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        com.goia.sict_backend.entity.User user = userRepo.findOneByEmail(email);
        if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
            throw new UsernameNotFoundException("User not found: " + email);
        }

        List<String> roleNames = userRoleRepo.findRoleNamesByUserEmail(email);

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String name : roleNames) {
            if (name != null && !name.isBlank()) {
                String normalized = name.startsWith("ROLE_") ? name : "ROLE_" + name.trim();
                authorities.add(new SimpleGrantedAuthority(normalized));
            }
        }

        boolean disabled = (user.getStatus() != null && user.getStatus() == 0);

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(disabled)
                .build();
    }
}
