package com.pm.scheduler.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pm.scheduler.common.constant.EquipmentStatus;
import com.pm.scheduler.common.constant.TriggerType;
import com.pm.scheduler.common.exception.BusinessException;
import com.pm.scheduler.dto.EquipmentCreateDTO;
import com.pm.scheduler.dto.EquipmentQueryDTO;
import com.pm.scheduler.dto.EquipmentUpdateDTO;
import com.pm.scheduler.dto.RuntimeRegisterDTO;
import com.pm.scheduler.entity.Equipment;
import com.pm.scheduler.entity.MaintenanceStandard;
import com.pm.scheduler.entity.RuntimeLog;
import com.pm.scheduler.entity.WorkOrder;
import com.pm.scheduler.mapper.EquipmentMapper;
import com.pm.scheduler.mapper.MaintenanceStandardMapper;
import com.pm.scheduler.mapper.RuntimeLogMapper;
import com.pm.scheduler.mapper.WorkOrderMapper;
import com.pm.scheduler.service.EquipmentService;
import com.pm.scheduler.service.WorkOrderService;
import com.pm.scheduler.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentMapper equipmentMapper;
    private final MaintenanceStandardMapper standardMapper;
    private final WorkOrderMapper workOrderMapper;
    private final RuntimeLogMapper runtimeLogMapper;
    private final WorkOrderService workOrderService;

    @Override
    public Page<EquipmentVO> page(EquipmentQueryDTO queryDTO) {
        Page<Equipment> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(Equipment::getCode, queryDTO.getKeyword())
                    .or().like(Equipment::getName, queryDTO.getKeyword())
                    .or().like(Equipment::getModel, queryDTO.getKeyword()));
        }
        if (StringUtils.hasText(queryDTO.getProductionLine())) {
            wrapper.eq(Equipment::getProductionLine, queryDTO.getProductionLine());
        }
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(Equipment::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(Equipment::getCreatedAt);

        Page<Equipment> equipmentPage = equipmentMapper.selectPage(page, wrapper);
        Page<EquipmentVO> voPage = new Page<>(equipmentPage.getCurrent(), equipmentPage.getSize(), equipmentPage.getTotal());
        List<EquipmentVO> voList = new ArrayList<>();
        for (Equipment equipment : equipmentPage.getRecords()) {
            EquipmentVO vo = toEquipmentVO(equipment);
            voList.add(vo);
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public EquipmentDetailVO getById(Long id) {
        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) {
            throw new BusinessException("设备不存在");
        }

        EquipmentDetailVO detailVO = new EquipmentDetailVO();
        BeanUtils.copyProperties(equipment, detailVO);
        fillMaintenanceInfo(detailVO, equipment);

        List<MaintenanceStandard> standards = standardMapper.selectList(
                new LambdaQueryWrapper<MaintenanceStandard>().eq(MaintenanceStandard::getEquipmentId, id));
        List<MaintenanceStandardVO> standardVOS = new ArrayList<>();
        for (MaintenanceStandard standard : standards) {
            MaintenanceStandardVO svo = new MaintenanceStandardVO();
            BeanUtils.copyProperties(standard, svo);
            svo.setEquipmentName(equipment.getName());
            standardVOS.add(svo);
        }
        detailVO.setStandards(standardVOS);

        List<WorkOrder> orders = workOrderMapper.selectList(
                new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getEquipmentId, id)
                        .orderByDesc(WorkOrder::getCreatedAt).last("LIMIT 10"));
        List<WorkOrderVO> orderVOS = new ArrayList<>();
        for (WorkOrder order : orders) {
            WorkOrderVO wvo = new WorkOrderVO();
            BeanUtils.copyProperties(order, wvo);
            orderVOS.add(wvo);
        }
        detailVO.setRecentOrders(orderVOS);

        List<RuntimeLog> logs = runtimeLogMapper.selectList(
                new LambdaQueryWrapper<RuntimeLog>().eq(RuntimeLog::getEquipmentId, id)
                        .orderByDesc(RuntimeLog::getCreatedAt).last("LIMIT 10"));
        List<RuntimeLogVO> logVOS = new ArrayList<>();
        for (RuntimeLog log : logs) {
            RuntimeLogVO lvo = new RuntimeLogVO();
            BeanUtils.copyProperties(log, lvo);
            logVOS.add(lvo);
        }
        detailVO.setRuntimeLogs(logVOS);

        return detailVO;
    }

    @Override
    public void create(EquipmentCreateDTO dto) {
        Equipment equipment = new Equipment();
        BeanUtils.copyProperties(dto, equipment);
        if (equipment.getStatus() == null) {
            equipment.setStatus(EquipmentStatus.NORMAL);
        }
        if (equipment.getCurrentRuntime() == null) {
            equipment.setCurrentRuntime(BigDecimal.ZERO);
        }
        equipment.setLastMaintenanceRuntime(BigDecimal.ZERO);
        equipment.setCreatedAt(LocalDateTime.now());
        equipment.setUpdatedAt(LocalDateTime.now());
        equipmentMapper.insert(equipment);
    }

    @Override
    public void update(Long id, EquipmentUpdateDTO dto) {
        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) {
            throw new BusinessException("设备不存在");
        }
        if (dto.getName() != null) equipment.setName(dto.getName());
        if (dto.getModel() != null) equipment.setModel(dto.getModel());
        if (dto.getProductionLine() != null) equipment.setProductionLine(dto.getProductionLine());
        if (dto.getInstallDate() != null) equipment.setInstallDate(dto.getInstallDate());
        if (dto.getStatus() != null) equipment.setStatus(dto.getStatus());
        equipment.setUpdatedAt(LocalDateTime.now());
        equipmentMapper.updateById(equipment);
    }

    @Override
    public void delete(Long id) {
        Long orderCount = workOrderMapper.selectCount(
                new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getEquipmentId, id));
        if (orderCount > 0) {
            throw new BusinessException("该设备存在关联工单，无法删除");
        }
        equipmentMapper.deleteById(id);
    }

    @Override
    public void registerRuntime(Long id, RuntimeRegisterDTO dto, Long operatorId) {
        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) {
            throw new BusinessException("设备不存在");
        }
        BigDecimal previousRuntime = equipment.getCurrentRuntime();
        if (dto.getRuntimeValue().compareTo(previousRuntime) < 0) {
            throw new BusinessException("运行时数不能小于当前值");
        }
        BigDecimal increment = dto.getRuntimeValue().subtract(previousRuntime);

        equipment.setCurrentRuntime(dto.getRuntimeValue());
        equipment.setUpdatedAt(LocalDateTime.now());
        equipmentMapper.updateById(equipment);

        RuntimeLog log = new RuntimeLog();
        log.setEquipmentId(id);
        log.setRuntimeValue(dto.getRuntimeValue());
        log.setIncrementValue(increment);
        log.setOperatorId(operatorId);
        log.setRecordedAt(dto.getRecordedAt() != null ? dto.getRecordedAt() : LocalDateTime.now());
        log.setCreatedAt(LocalDateTime.now());
        runtimeLogMapper.insert(log);

        List<MaintenanceStandard> standards = standardMapper.selectList(
                new LambdaQueryWrapper<MaintenanceStandard>()
                        .eq(MaintenanceStandard::getEquipmentId, id)
                        .eq(MaintenanceStandard::getTriggerType, TriggerType.RUNTIME)
                        .eq(MaintenanceStandard::getEnabled, 1));
        for (MaintenanceStandard standard : standards) {
            BigDecimal targetRuntime = equipment.getLastMaintenanceRuntime().add(standard.getCycleHours());
            BigDecimal remindRuntime = targetRuntime.subtract(standard.getRemindHoursBefore());
            if (dto.getRuntimeValue().compareTo(remindRuntime) >= 0) {
                workOrderService.autoGenerate(id, standard.getId(), TriggerType.RUNTIME);
            }
        }
    }

    private EquipmentVO toEquipmentVO(Equipment equipment) {
        EquipmentVO vo = new EquipmentVO();
        BeanUtils.copyProperties(equipment, vo);
        fillMaintenanceInfo(vo, equipment);
        return vo;
    }

    private void fillMaintenanceInfo(EquipmentVO vo, Equipment equipment) {
        List<MaintenanceStandard> standards = standardMapper.selectList(
                new LambdaQueryWrapper<MaintenanceStandard>()
                        .eq(MaintenanceStandard::getEquipmentId, equipment.getId())
                        .eq(MaintenanceStandard::getEnabled, 1));

        LocalDate earliestDate = null;
        for (MaintenanceStandard standard : standards) {
            LocalDate nextDate = calculateNextDate(equipment, standard);
            if (nextDate != null && (earliestDate == null || nextDate.isBefore(earliestDate))) {
                earliestDate = nextDate;
            }
        }

        vo.setNextMaintenanceDate(earliestDate);
        if (earliestDate != null) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), earliestDate);
            vo.setDaysUntilNext((int) days);
            if (days < 0) {
                vo.setMaintenanceStatus("OVERDUE");
            } else if (days <= 3) {
                vo.setMaintenanceStatus("UPCOMING");
            } else {
                vo.setMaintenanceStatus("NORMAL");
            }
        } else {
            vo.setMaintenanceStatus("NORMAL");
        }
    }

    private LocalDate calculateNextDate(Equipment equipment, MaintenanceStandard standard) {
        if (TriggerType.CALENDAR.equals(standard.getTriggerType()) && standard.getCycleDays() != null) {
            LocalDate lastDate = equipment.getLastMaintenanceDate();
            if (lastDate == null) {
                lastDate = equipment.getInstallDate();
            }
            if (lastDate == null) {
                return null;
            }
            return lastDate.plusDays(standard.getCycleDays());
        }
        return null;
    }
}
