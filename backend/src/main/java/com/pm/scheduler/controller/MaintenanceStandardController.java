package com.pm.scheduler.controller;

import com.pm.scheduler.common.result.R;
import com.pm.scheduler.dto.StandardCreateDTO;
import com.pm.scheduler.dto.StandardUpdateDTO;
import com.pm.scheduler.service.MaintenanceStandardService;
import com.pm.scheduler.vo.MaintenanceStandardVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance-standard")
@RequiredArgsConstructor
public class MaintenanceStandardController {

    private final MaintenanceStandardService standardService;

    @GetMapping("/equipment/{equipmentId}")
    public R<List<MaintenanceStandardVO>> listByEquipmentId(@PathVariable Long equipmentId) {
        return R.ok(standardService.listByEquipmentId(equipmentId));
    }

    @PostMapping
    public R<Void> create(@Valid @RequestBody StandardCreateDTO dto) {
        standardService.create(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody StandardUpdateDTO dto) {
        standardService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        standardService.delete(id);
        return R.ok();
    }
}
