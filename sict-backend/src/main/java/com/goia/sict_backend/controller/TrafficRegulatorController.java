package com.goia.sict_backend.controller;

import com.goia.sict_backend.dto.TrafficRegulatorDTO;
import com.goia.sict_backend.entity.TrafficRegulator;
import com.goia.sict_backend.service.ITrafficRegulatorService;
import com.goia.sict_backend.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/traffic-regulators")
@RequiredArgsConstructor
public class TrafficRegulatorController {
    private final ITrafficRegulatorService trafficRegulatorService;
    private final MapperUtil mapperUtil;

    @GetMapping
    @PreAuthorize("@authorizeLogic.hasAccess('findAll')")
    public ResponseEntity<List<TrafficRegulatorDTO>> findAll() throws Exception {
        List<TrafficRegulator> entities = trafficRegulatorService.findAll();
        List<TrafficRegulatorDTO> dtoList = mapperUtil.mapList(entities, TrafficRegulatorDTO.class);
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authorizeLogic.hasAccess('findById')")
    public ResponseEntity<TrafficRegulatorDTO> findById(@PathVariable UUID id) throws Exception {
        TrafficRegulator entity = trafficRegulatorService.findById(id);
        TrafficRegulatorDTO dto = mapperUtil.map(entity, TrafficRegulatorDTO.class);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("@authorizeLogic.hasAccess('save')")
    public ResponseEntity<TrafficRegulatorDTO> save(@RequestBody TrafficRegulatorDTO dto) throws Exception {
        TrafficRegulator entity = mapperUtil.map(dto, TrafficRegulator.class);
        TrafficRegulator saved = trafficRegulatorService.save(entity);
        TrafficRegulatorDTO out = mapperUtil.map(saved, TrafficRegulatorDTO.class);
        return ResponseEntity.ok(out);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@authorizeLogic.hasAccess('update')")
    public ResponseEntity<TrafficRegulatorDTO> update(@PathVariable UUID id,
                                                      @RequestBody TrafficRegulatorDTO dto) throws Exception {
        TrafficRegulator entity = mapperUtil.map(dto, TrafficRegulator.class);
        TrafficRegulator updated = trafficRegulatorService.update(entity, id);
        TrafficRegulatorDTO out = mapperUtil.map(updated, TrafficRegulatorDTO.class);
        return ResponseEntity.ok(out);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@authorizeLogic.hasAccess('delete')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) throws Exception {
        trafficRegulatorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
