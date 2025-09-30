package com.goia.sict_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserListRoleDTO {
    private UserDTO user;
    private List<RoleDTO> roles;
}
