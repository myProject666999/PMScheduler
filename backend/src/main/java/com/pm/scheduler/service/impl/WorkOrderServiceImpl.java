package com.pm.scheduler.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pm.scheduler.common.constant.Constants;
import com.pm.scheduler.common.constant.WorkOrderStatus;
import com.pm.scheduler.common.exception.BusinessException;
import com.pm.scheduler.dto.WorkOrderDispatchDTO;
import com.pm.scheduler.dto.WorkOrderExecuteDTO;
import com.pm.scheduler.dto.WorkOrderQueryDTO;
import com.pm.scheduler.dto.WorkOrderReviewDTO;
import com.pm.scheduler.entity.*;
import com.pm.scheduler.mapper.*;
import com.pm.scheduler.service.WorkOrderService;
import com.pm.scheduler.vo.WorkOrderPartVO;
import com.pm.scheduler.vo.WorkOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderPartMapper workOrderPartMapper;
    private final EquipmentMapper equipmentMapper;
    private final MaintenanceStandardMapper standardMapper;
    private final SysUserMapper sysUserMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Page<WorkOrderVO> page(WorkOrderQueryDTO queryDTO) {
        Page<WorkOrder> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(WorkOrder::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getEquipmentId() != null) {
            wrapper.eq(WorkOrder::getEquipmentId, queryDTO.getEquipmentId());
        }
        if (queryDTO.getExecuteUserId() != null) {
            wrapper.eq(WorkOrder::getExecuteUserId, queryDTO.getExecuteUserId());
        }
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(WorkOrder::getPlanDate, queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(WorkOrder::getPlanDate, queryDTO.getEndDate());
        }
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.like(WorkOrder::getOrderNo, queryDTO.getKeyword());
        }
        wrapper.orderByDesc(WorkOrder::getCreatedAt);

        Page<WorkOrder> orderPage = workOrderMapper.selectPage(page, wrapper);
        Page<WorkOrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        List<WorkOrderVO> voList = new ArrayList<>();
        for (WorkOrder order : orderPage.getRecords()) {
            voList.add(toWorkOrderVO(order));
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public WorkOrderVO getById(Long id) {
        WorkOrder order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        return toWorkOrderVO(order);
    }

    @Override
    public WorkOrder create(WorkOrder workOrder) {
        workOrder.setOrderNo(generateOrderNo());
        workOrder.setCreatedAt(LocalDateTime.now());
        workOrder.setUpdatedAt(LocalDateTime.now());
        workOrderMapper.insert(workOrder);
        return workOrder;
    }

    @Override
    public void dispatch(Long id, WorkOrderDispatchDTO dto, Long userId) {
        WorkOrder order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        if (!WorkOrderStatus.PENDING.equals(order.getStatus())) {
            throw new BusinessException("工单状态不允许派工");
        }
        order.setStatus(WorkOrderStatus.DISPATCHED);
        order.setDispatchUserId(userId);
        order.setExecuteUserId(dto.getExecuteUserId());
        order.setPlanDate(dto.getPlanDate());
        order.setDispatchAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        workOrderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void execute(Long id, WorkOrderExecuteDTO dto) {
        WorkOrder order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        if (!WorkOrderStatus.EXECUTING.equals(order.getStatus())) {
            throw new BusinessException("工单状态不允许执行回填");
        }
        order.setStatus(WorkOrderStatus.REVIEWING);
        order.setActualDate(LocalDate.now());
        if (dto.getActualHours() != null) {
            order.setActualHours(dto.getActualHours());
        }
        if (dto.getMaintenanceContent() != null) {
            order.setMaintenanceContent(dto.getMaintenanceContent());
        }
        if (dto.getRemark() != null) {
            order.setRemark(dto.getRemark());
        }
        order.setExecuteAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        workOrderMapper.updateById(order);

        if (dto.getParts() != null) {
            for (var partDTO : dto.getParts()) {
                WorkOrderPart part = new WorkOrderPart();
                part.setWorkOrderId(id);
                part.setPartName(partDTO.getPartName());
                part.setPartCode(partDTO.getPartCode());
                part.setQuantity(partDTO.getQuantity());
                part.setUnit(partDTO.getUnit());
                part.setCreatedAt(LocalDateTime.now());
                workOrderPartMapper.insert(part);
            }
        }
    }

    @Override
    @Transactional
    public void review(Long id, WorkOrderReviewDTO dto, Long userId) {
        WorkOrder order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        if (!WorkOrderStatus.REVIEWING.equals(order.getStatus())) {
            throw new BusinessException("工单状态不允许审核");
        }
        if (dto.getApproved()) {
            order.setStatus(WorkOrderStatus.COMPLETED);
            order.setCompleteAt(LocalDateTime.now());

            Equipment equipment = equipmentMapper.selectById(order.getEquipmentId());
            if (equipment != null) {
                equipment.setLastMaintenanceDate(LocalDate.now());
                if (equipment.getCurrentRuntime() != null) {
                    equipment.setLastMaintenanceRuntime(equipment.getCurrentRuntime());
                }
                equipment.setUpdatedAt(LocalDateTime.now());
                equipmentMapper.updateById(equipment);
            }
        } else {
            order.setStatus(WorkOrderStatus.EXECUTING);
        }
        if (dto.getRemark() != null) {
            order.setRemark(dto.getRemark());
        }
        order.setUpdatedAt(LocalDateTime.now());
        workOrderMapper.updateById(order);
    }

    @Override
    public WorkOrder autoGenerate(Long equipmentId, Long standardId, String triggerType) {
        String lockKey = Constants.ORDER_LOCK_PREFIX + equipmentId + ":" + standardId;
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 60, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            return null;
        }
        try {
            Long existCount = workOrderMapper.selectCount(
                    new LambdaQueryWrapper<WorkOrder>()
                            .eq(WorkOrder::getEquipmentId, equipmentId)
                            .eq(WorkOrder::getStandardId, standardId)
                            .ne(WorkOrder::getStatus, WorkOrderStatus.COMPLETED)
                            .ne(WorkOrder::getStatus, WorkOrderStatus.REJECTED));
            if (existCount > 0) {
                return null;
            }

            WorkOrder order = new WorkOrder();
            order.setEquipmentId(equipmentId);
            order.setStandardId(standardId);
            order.setTriggerType(triggerType);
            order.setStatus(WorkOrderStatus.PENDING);
            order.setPlanDate(LocalDate.now());
            return create(order);
        } finally {
            stringRedisTemplate.delete(lockKey);
        }
    }

    private WorkOrderVO toWorkOrderVO(WorkOrder order) {
        WorkOrderVO vo = new WorkOrderVO();
        BeanUtils.copyProperties(order, vo);

        Equipment equipment = equipmentMapper.selectById(order.getEquipmentId());
        if (equipment != null) {
            vo.setEquipmentName(equipment.getName());
            vo.setEquipmentCode(equipment.getCode());
        }

        MaintenanceStandard standard = standardMapper.selectById(order.getStandardId());
        if (standard != null) {
            vo.setStandardItemName(standard.getItemName());
        }

        if (order.getExecuteUserId() != null) {
            SysUser executeUser = sysUserMapper.selectById(order.getExecuteUserId());
            if (executeUser != null) {
                vo.setExecuteUserName(executeUser.getRealName());
            }
        }

        if (order.getDispatchUserId() != null) {
            SysUser dispatchUser = sysUserMapper.selectById(order.getDispatchUserId());
            if (dispatchUser != null) {
                vo.setDispatchUserName(dispatchUser.getRealName());
            }
        }

        List<WorkOrderPart> parts = workOrderPartMapper.selectList(
                new LambdaQueryWrapper<WorkOrderPart>().eq(WorkOrderPart::getWorkOrderId, order.getId()));
        List<WorkOrderPartVO> partVOS = new ArrayList<>();
        for (WorkOrderPart part : parts) {
            WorkOrderPartVO pvo = new WorkOrderPartVO();
            BeanUtils.copyProperties(part, pvo);
            partVOS.add(pvo);
        }
        vo.setParts(partVOS);

        return vo;
    }

    private String generateOrderNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "WO-" + dateStr + "-";
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(WorkOrder::getOrderNo, prefix).orderByDesc(WorkOrder::getOrderNo).last("LIMIT 1");
        WorkOrder lastOrder = workOrderMapper.selectOne(wrapper);
        int seq = 1;
        if (lastOrder != null && lastOrder.getOrderNo() != null) {
            String lastSeq = lastOrder.getOrderNo().substring(prefix.length());
            seq = Integer.parseInt(lastSeq) + 1;
        }
        return prefix + String.format("%03d", seq);
    }
}
