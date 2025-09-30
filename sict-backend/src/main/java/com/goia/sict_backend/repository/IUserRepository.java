package com.goia.sict_backend.repository;

import com.goia.sict_backend.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IUserRepository extends IGenericRepository<User, UUID> {
    User findOneByEmail(String email);
    @Transactional
    @Modifying
    @Query("UPDATE User us SET us.password = :password WHERE us.fullName = :fullName")
    void changePassword(@Param("fullName") String username, @Param("password") String newPassword);

    @Query("SELECT ur.user FROM UserRole ur WHERE ur.role.name = :roleName AND ur.user.status = 1 AND ur.status = 1")
    List<User> findUsersByRoleNameAndStatus(@Param("roleName") String roleName);

    boolean existsByEmailAndIdUserNot(String email, UUID idUser);

    Optional<User> findByEmail(String email);


}
