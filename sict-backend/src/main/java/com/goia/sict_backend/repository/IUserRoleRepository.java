package com.goia.sict_backend.repository;


import com.goia.sict_backend.entity.UserRole;
import com.goia.sict_backend.entity.UserRolePK;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IUserRoleRepository extends IGenericRepository<UserRole, UserRolePK>{

    @Modifying
    @Query(value = """
    INSERT INTO user_role(id_user, id_role, status)
    VALUES (:idUser, :idRole, 1)
    ON CONFLICT (id_user, id_role)
    DO UPDATE SET status = 1
            """, nativeQuery = true)
    void saveRole(@Param("idUser") UUID idUser, @Param("idRole") UUID idRole);

    @Modifying
    @Query(value = "DELETE FROM user_role WHERE id_user = :idUser", nativeQuery = true)
    void deleteByUserId(@Param("idUser") UUID idUser);

    @Modifying
    @Query(value = "UPDATE user_role SET status = 0 WHERE id_user = :idUser", nativeQuery = true)
    void softDeleteByUserId(@Param("idUser") UUID idUser);

    @Query("SELECT ur FROM UserRole ur WHERE ur.user.idUser = :idUser AND ur.status = 1")
    List<UserRole> findActiveRolesByUserId(@Param("idUser") UUID idUser);
    boolean existsByUser_IdUserAndRole_IdRole(UUID userId, UUID roleId);

    @Query("""
        select r.name
        from UserRole ur
        join ur.user u
        join ur.role r
        where u.email = :email
    """)
    List<String> findRoleNamesByUserEmail(@Param("email") String email);

    @Query("""
        select r.name
        from UserRole ur
        join ur.user u
        join ur.role r
        where u.idUser = :idUser
    """)
    List<String> findRoleNamesByUserId(@Param("idUser") UUID idUser);

}
