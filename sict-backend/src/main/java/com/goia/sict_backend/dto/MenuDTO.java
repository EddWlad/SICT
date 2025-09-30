package com.goia.sict_backend.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MenuDTO {
    @EqualsAndHashCode.Include
    private Integer idMenu;
    private String icon;
    private String name;
    private String url;
    private Integer status = 1;
}

