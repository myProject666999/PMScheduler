package com.pm.scheduler.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pm.scheduler.dto.EquipmentCreateDTO;
import com.pm.scheduler.dto.EquipmentQueryDTO;
import com.pm.scheduler.dto.EquipmentUpdateDTO;
import com.pm.scheduler.dto.RuntimeRegisterDTO;
import com.pm.scheduler.vo.EquipmentDetailVO;
import com.pm.scheduler.vo.EquipmentVO;

public interface EquipmentService {

    Page<EquipmentVO> page(EquipmentQueryDTO queryDTO);

    EquipmentDetailVO getById(Long id);

    void create(EquipmentCreateDTO dto);

    void update(Long id, EquipmentUpdateDTO dto);

    void delete(Long id);

    void registerRuntime(Long id, RuntimeRegisterDTO dto, Long operatorId);
}
