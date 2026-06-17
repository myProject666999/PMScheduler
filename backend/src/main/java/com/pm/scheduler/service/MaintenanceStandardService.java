package com.pm.scheduler.service;

import com.pm.scheduler.dto.StandardCreateDTO;
import com.pm.scheduler.dto.StandardUpdateDTO;
import com.pm.scheduler.vo.MaintenanceStandardVO;

import java.util.List;

public interface MaintenanceStandardService {

    List<MaintenanceStandardVO> listByEquipmentId(Long equipmentId);

    void create(StandardCreateDTO dto);

    void update(Long id, StandardUpdateDTO dto);

    void delete(Long id);
}
