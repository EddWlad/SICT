package com.goia.sict_backend.controller;

import com.goia.sict_backend.comm.MCommandType;
import com.goia.sict_backend.dto.*;
import com.goia.sict_backend.entity.CommProfile;
import com.goia.sict_backend.entity.TrafficRegulator;
import com.goia.sict_backend.service.ICommProfileService;
import com.goia.sict_backend.service.ICommTestService;
import com.goia.sict_backend.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/comm-profiles")
@RequiredArgsConstructor
public class CommProfileController {
    private final ICommProfileService commProfileService;
    private final ICommTestService commTestService;
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

    // ====== NUEVO: Probar conexión RS-232 del CommProfile ======
    @PostMapping("/{id}/test")
    @PreAuthorize("@authorizeLogic.hasAccess('findById')")
    public ResponseEntity<CommTestResultDTO> testConnection(@PathVariable UUID id) throws Exception {
        CommTestResultDTO result = commTestService.testConnection(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/test-frame1")
    @PreAuthorize("@authorizeLogic.hasAccess('findById')") // o el permiso que uses
    public ResponseEntity<CommTestResultDTO> testFrame(
            @PathVariable UUID id,
            @RequestBody CommTestRequestDTO req
    ) throws Exception {
        CommTestResultDTO result = commTestService.testFrame1(id, req);
        return ResponseEntity.ok(result);
    }

    /**
     * Enviar trama en DEC (protocolo M) y leer respuesta.
     * Recomendado proteger con un rol/admin o activar solo en perfil 'dev'.
     */
    @PostMapping("/{id}/test-frame")
    @PreAuthorize("@authorizeLogic.hasAccess('findById')") // ajusta si usas otro permiso
    public ResponseEntity<CommTestResultDTO> testFrame(
            @PathVariable UUID id,
            @RequestBody CommFrameRequestDTO req
    ) throws Exception {
        CommTestResultDTO result = commTestService.testFrame(id, req);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/m/send")
    @PreAuthorize("@authorizeLogic.hasAccess('findById')") // ajusta si usas otro permiso
    public ResponseEntity<CommTestResultDTO> sendM(
            @PathVariable UUID id,
            @RequestBody ProtocolMCommandDTO cmd
    ) throws Exception {
        var result = commTestService.sendMCommand(id, cmd);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/m/commands/{type}")
    @PreAuthorize("@authorizeLogic.hasAccess('findById')") // ajusta si usas otro permiso
    public ResponseEntity<CommTestResultDTO> sendMCommandByType(
            @PathVariable("id") UUID idCommProfile,
            @PathVariable("type") String type,
            @RequestBody(required = false) Map<String,Object> args
    ) throws Exception {
        MCommandType ct = MCommandType.valueOf(type.toUpperCase());
        CommTestResultDTO res = commTestService.sendMCommandType(idCommProfile, ct, args);
        return ResponseEntity.ok(res);
    }
}
