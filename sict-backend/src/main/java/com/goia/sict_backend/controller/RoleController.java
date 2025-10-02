package com.goia.sict_backend.controller;

import com.goia.sict_backend.dto.RoleDTO;
import com.goia.sict_backend.entity.Role;
import com.goia.sict_backend.service.IRoleService;
import com.goia.sict_backend.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {
    private final IRoleService roleService;
    private final MapperUtil mapperUtil;

    @PreAuthorize("@authorizeLogic.hasAccess('findAll')")
    @GetMapping
    public ResponseEntity<List<RoleDTO>> findAll() throws Exception {
        List<RoleDTO> list = mapperUtil.mapList(roleService.findAll(), RoleDTO.class);
        return ResponseEntity.ok(list);
    }
    @PreAuthorize("@authorizeLogic.hasAccess('findById')")
    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> findById(@PathVariable("id") String id) throws Exception {
        RoleDTO dto = mapperUtil.map(roleService.findById(UUID.fromString(id)), RoleDTO.class);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("@authorizeLogic.hasAccess('save')")
    @PostMapping
    public ResponseEntity<RoleDTO> save(@RequestBody RoleDTO roleDTO) throws Exception {
        Role obj = roleService.save(mapperUtil.map(roleDTO, Role.class));

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(obj.getIdRole())
                .toUri();

        return ResponseEntity.ok(mapperUtil.map(obj, RoleDTO.class));
    }

    @PreAuthorize("@authorizeLogic.hasAccess('update')")
    @PutMapping("/{id}")
    public ResponseEntity<RoleDTO> update(@PathVariable("id") String id, @RequestBody RoleDTO roleDTO) throws Exception {
        Role obj = roleService.update(mapperUtil.map(roleDTO, Role.class), UUID.fromString(id));
        return ResponseEntity.ok(mapperUtil.map(obj, RoleDTO.class));
    }

    @PreAuthorize("@authorizeLogic.hasAccess('delete')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) throws Exception {
        roleService.delete(UUID.fromString(id));
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@authorizeLogic.hasAccess('pageable')")
    @GetMapping("/pageable")
    public ResponseEntity<Page<Role>> listPage(Pageable pageable) throws Exception {
        Page<Role> page = roleService.listPage(pageable);
        return ResponseEntity.ok(page);
    }
}
