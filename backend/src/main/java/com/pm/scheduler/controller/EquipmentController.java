package com.pm.scheduler.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pm.scheduler.common.result.R;
import com.pm.scheduler.dto.EquipmentCreateDTO;
import com.pm.scheduler.dto.EquipmentQueryDTO;
import com.pm.scheduler.dto.EquipmentUpdateDTO;
import com.pm.scheduler.dto.RuntimeRegisterDTO;
import com.pm.scheduler.service.EquipmentService;
import com.pm.scheduler.vo.EquipmentDetailVO;
import com.pm.scheduler.vo.EquipmentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping("/page")
    public R<Page<EquipmentVO>> page(EquipmentQueryDTO queryDTO) {
        return R.ok(equipmentService.page(queryDTO));
    }

    @GetMapping("/{id}")
    public R<EquipmentDetailVO> getById(@PathVariable Long id) {
        return R.ok(equipmentService.getById(id));
    }

    @PostMapping
    public R<Void> create(@Valid @RequestBody EquipmentCreateDTO dto) {
        equipmentService.create(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody EquipmentUpdateDTO dto) {
        equipmentService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        equipmentService.delete(id);
        return R.ok();
    }

    @PostMapping("/{id}/runtime")
    public R<Void> registerRuntime(@PathVariable Long id, @Valid @RequestBody RuntimeRegisterDTO dto) {
        equipmentService.registerRuntime(id, dto, 1L);
        return R.ok();
    }
}
