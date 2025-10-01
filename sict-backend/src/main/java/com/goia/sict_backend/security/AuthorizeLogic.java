package com.goia.sict_backend.security;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthorizeLogic {

    public boolean hasAccess(String path){
        boolean result = false;

        String methodRole = switch (path){
            case "findAll", "findById", "save", "update", "delete",
                 "findUserDetail", "findActiveAdmins", "findActiveOperators" -> "ROLE_ADMINISTRADOR";
            case "findByUserTasks", "updateTask", "deleteTask", "pageable" -> "ROLE_OPERADOR,ROLE_ADMINISTRADOR";
            default -> "ROLE_SUPER_USUARIO";
        };

        String[] methodRoles = methodRole.split(",");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Email is: {}", auth.getName());

        for (GrantedAuthority ga : auth.getAuthorities()) {
            String roleUser = ga.getAuthority();
            log.info("Role is: {}", roleUser);

            for(String role : methodRoles){
                if(roleUser.equalsIgnoreCase(role)){
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
}
