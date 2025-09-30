package com.goia.sict_backend.dto;


import lombok.*;

import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RoleDTO {
    @EqualsAndHashCode.Include
    private UUID idRole;
    private String name;
    private String description;
    private Integer status = 1;
}
