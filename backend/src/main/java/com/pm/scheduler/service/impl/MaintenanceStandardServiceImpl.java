package com.pm.scheduler.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pm.scheduler.common.exception.BusinessException;
import com.pm.scheduler.dto.StandardCreateDTO;
import com.pm.scheduler.dto.StandardUpdateDTO;
import com.pm.scheduler.entity.Equipment;
import com.pm.scheduler.entity.MaintenanceStandard;
import com.pm.scheduler.entity.WorkOrder;
import com.pm.scheduler.mapper.EquipmentMapper;
import com.pm.scheduler.mapper.MaintenanceStandardMapper;
import com.pm.scheduler.mapper.WorkOrderMapper;
import com.pm.scheduler.service.MaintenanceStandardService;
import com.pm.scheduler.vo.MaintenanceStandardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceStandardServiceImpl implements MaintenanceStandardService {

    private final MaintenanceStandardMapper standardMapper;
    private final EquipmentMapper equipmentMapper;
    private final WorkOrderMapper workOrderMapper;

    @Override
    public List<MaintenanceStandardVO> listByEquipmentId(Long equipmentId) {
        List<MaintenanceStandard> standards = standardMapper.selectList(
                new LambdaQueryWrapper<MaintenanceStandard>().eq(MaintenanceStandard::getEquipmentId, equipmentId));
        Equipment equipment = equipmentMapper.selectById(equipmentId);
        List<MaintenanceStandardVO> voList = new ArrayList<>();
        for (MaintenanceStandard standard : standards) {
            MaintenanceStandardVO vo = new MaintenanceStandardVO();
            BeanUtils.copyProperties(standard, vo);
            if (equipment != null) {
                vo.setEquipmentName(equipment.getName());
            }
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public void create(StandardCreateDTO dto) {
        Equipment equipment = equipmentMapper.selectById(dto.getEquipmentId());
        if (equipment == null) {
            throw new BusinessException("设备不存在");
        }
        MaintenanceStandard standard = new MaintenanceStandard();
        BeanUtils.copyProperties(dto, standard);
        standard.setCreatedAt(LocalDateTime.now());
        standard.setUpdatedAt(LocalDateTime.now());
        standardMapper.insert(standard);
    }

    @Override
    public void update(Long id, StandardUpdateDTO dto) {
        MaintenanceStandard standard = standardMapper.selectById(id);
        if (standard == null) {
            throw new BusinessException("保养标准不存在");
        }
        if (dto.getItemName() != null) standard.setItemName(dto.getItemName());
        if (dto.getTriggerType() != null) standard.setTriggerType(dto.getTriggerType());
        if (dto.getCycleDays() != null) standard.setCycleDays(dto.getCycleDays());
        if (dto.getCycleHours() != null) standard.setCycleHours(dto.getCycleHours());
        if (dto.getRemindDaysBefore() != null) standard.setRemindDaysBefore(dto.getRemindDaysBefore());
        if (dto.getRemindHoursBefore() != null) standard.setRemindHoursBefore(dto.getRemindHoursBefore());
        if (dto.getContent() != null) standard.setContent(dto.getContent());
        if (dto.getEnabled() != null) standard.setEnabled(dto.getEnabled());
        standard.setUpdatedAt(LocalDateTime.now());
        standardMapper.updateById(standard);
    }

    @Override
    public void delete(Long id) {
        Long orderCount = workOrderMapper.selectCount(
                new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStandardId, id));
        if (orderCount > 0) {
            throw new BusinessException("该标准存在关联工单，无法删除");
        }
        standardMapper.deleteById(id);
    }
}
