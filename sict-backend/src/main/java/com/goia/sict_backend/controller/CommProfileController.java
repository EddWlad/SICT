package com.goia.sict_backend.controller;

import com.goia.sict_backend.dto.CommProfileDTO;
import com.goia.sict_backend.entity.CommProfile;
import com.goia.sict_backend.entity.TrafficRegulator;
import com.goia.sict_backend.service.ICommProfileService;
import com.goia.sict_backend.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/comm-profiles")
@RequiredArgsConstructor
public class CommProfileController {
    private final ICommProfileService commProfileService;
    private final MapperUtil mapperUtil;

    @GetMapping
    @PreAuthorize("@authorizeLogic.hasAccess('findAll')")
    public ResponseEntity<List<CommProfileDTO>> findAll() throws Exception {
        List<CommProfile> entities = commProfileService.findAll(); // activos e inactivos, sin borrados
        List<CommProfileDTO> dtoList = mapperUtil.mapList(entities, CommProfileDTO.class);
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authorizeLogic.hasAccess('findById')")
    public ResponseEntity<CommProfileDTO> findById(@PathVariable UUID id) throws Exception {
        CommProfile entity = commProfileService.findById(id);
        CommProfileDTO dto = mapperUtil.map(entity, CommProfileDTO.class);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/by-regulator/{idReg}")
    @PreAuthorize("@authorizeLogic.hasAccess('findAll')")
    public ResponseEntity<List<CommProfileDTO>> findByRegulator(@PathVariable UUID idReg) throws Exception {
        List<CommProfile> entities = commProfileService.findByRegulator(idReg);
        List<CommProfileDTO> dtoList = mapperUtil.mapList(entities, CommProfileDTO.class);
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/exists-active/{idReg}")
    @PreAuthorize("@authorizeLogic.hasAccess('findById')")
    public ResponseEntity<Boolean> existsActive(@PathVariable UUID idReg) throws Exception {
        boolean exists = commProfileService.existsActiveForRegulator(idReg);
        return ResponseEntity.ok(exists);
    }

    @PostMapping
    @PreAuthorize("@authorizeLogic.hasAccess('save')")
    public ResponseEntity<CommProfileDTO> save(@RequestBody CommProfileDTO dto) throws Exception {
        CommProfile entity = mapperUtil.map(dto, CommProfile.class);

        TrafficRegulator tr = new TrafficRegulator();
        tr.setIdTrafficRegulator(dto.getIdTrafficRegulator());
        entity.setTrafficRegulator(tr);

        CommProfile saved = commProfileService.save(entity);
        CommProfileDTO out = mapperUtil.map(saved, CommProfileDTO.class);
        return ResponseEntity.ok(out);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@authorizeLogic.hasAccess('update')")
    public ResponseEntity<CommProfileDTO> update(@PathVariable UUID id,
                                                 @RequestBody CommProfileDTO dto) throws Exception {
        CommProfile entity = mapperUtil.map(dto, CommProfile.class);

        TrafficRegulator tr = new TrafficRegulator();
        tr.setIdTrafficRegulator(dto.getIdTrafficRegulator());
        entity.setTrafficRegulator(tr);

        CommProfile updated = commProfileService.update(entity, id);
        CommProfileDTO out = mapperUtil.map(updated, CommProfileDTO.class);
        return ResponseEntity.ok(out);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@authorizeLogic.hasAccess('delete')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) throws Exception {
        commProfileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
