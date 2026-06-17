package com.pm.scheduler.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pm.scheduler.common.constant.Constants;
import com.pm.scheduler.common.constant.TriggerType;
import com.pm.scheduler.common.constant.WorkOrderStatus;
import com.pm.scheduler.entity.Equipment;
import com.pm.scheduler.entity.MaintenanceStandard;
import com.pm.scheduler.entity.WorkOrder;
import com.pm.scheduler.mapper.EquipmentMapper;
import com.pm.scheduler.mapper.MaintenanceStandardMapper;
import com.pm.scheduler.mapper.WorkOrderMapper;
import com.pm.scheduler.service.DashboardService;
import com.pm.scheduler.vo.DashboardStatsVO;
import com.pm.scheduler.vo.ReminderVO;
import com.pm.scheduler.mapper.SysUserMapper;
import com.pm.scheduler.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final EquipmentMapper equipmentMapper;
    private final MaintenanceStandardMapper standardMapper;
    private final WorkOrderMapper workOrderMapper;
    private final SysUserMapper sysUserMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public List<ReminderVO> getReminders() {
        List<ReminderVO> overdueReminders = (List<ReminderVO>) redisTemplate.opsForValue().get(Constants.REMINDER_OVERDUE_KEY);
        List<ReminderVO> upcomingReminders = (List<ReminderVO>) redisTemplate.opsForValue().get(Constants.REMINDER_UPCOMING_KEY);
        if (overdueReminders == null && upcomingReminders == null) {
            return calculateReminders();
        }
        List<ReminderVO> result = new ArrayList<>();
        if (overdueReminders != null) {
            result.addAll(overdueReminders);
        }
        if (upcomingReminders != null) {
            result.addAll(upcomingReminders);
        }
        return result;
    }

    @Override
    public DashboardStatsVO getStats() {
        DashboardStatsVO stats = new DashboardStatsVO();

        List<Equipment> equipments = equipmentMapper.selectList(null);
        stats.setTotalEquipment(equipments.size());
        int normalCount = 0, upcomingCount = 0, overdueCount = 0, maintenanceCount = 0;
        for (Equipment eq : equipments) {
            String ms = calculateEquipmentMaintenanceStatus(eq);
            switch (ms) {
                case "NORMAL" -> normalCount++;
                case "UPCOMING" -> upcomingCount++;
                case "OVERDUE" -> overdueCount++;
            }
            if ("MAINTENANCE".equals(eq.getStatus())) {
                maintenanceCount++;
            }
        }
        stats.setNormalCount(normalCount);
        stats.setUpcomingCount(upcomingCount);
        stats.setOverdueCount(overdueCount);
        stats.setMaintenanceCount(maintenanceCount);

        stats.setPendingOrders(workOrderMapper.selectCount(
                new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStatus, WorkOrderStatus.PENDING)).intValue());
        stats.setDispatchedOrders(workOrderMapper.selectCount(
                new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStatus, WorkOrderStatus.DISPATCHED)).intValue());
        stats.setExecutingOrders(workOrderMapper.selectCount(
                new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStatus, WorkOrderStatus.EXECUTING)).intValue());
        stats.setReviewingOrders(workOrderMapper.selectCount(
                new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStatus, WorkOrderStatus.REVIEWING)).intValue());

        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        Long totalMonth = workOrderMapper.selectCount(
                new LambdaQueryWrapper<WorkOrder>().ge(WorkOrder::getCreatedAt, monthStart.atStartOfDay()));
        Long completedMonth = workOrderMapper.selectCount(
                new LambdaQueryWrapper<WorkOrder>()
                        .eq(WorkOrder::getStatus, WorkOrderStatus.COMPLETED)
                        .ge(WorkOrder::getCompleteAt, monthStart.atStartOfDay()));
        if (totalMonth != null && totalMonth > 0) {
            stats.setMonthlyCompletionRate(BigDecimal.valueOf(completedMonth)
                    .divide(BigDecimal.valueOf(totalMonth), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        } else {
            stats.setMonthlyCompletionRate(BigDecimal.ZERO);
        }

        return stats;
    }

    @Override
    public void refreshReminders() {
        List<ReminderVO> allReminders = calculateReminders();
        List<ReminderVO> overdueList = new ArrayList<>();
        List<ReminderVO> upcomingList = new ArrayList<>();
        for (ReminderVO r : allReminders) {
            if (r.getOverdueDays() != null && r.getOverdueDays() > 0) {
                overdueList.add(r);
            } else {
                upcomingList.add(r);
            }
        }
        redisTemplate.opsForValue().set(Constants.REMINDER_OVERDUE_KEY, overdueList, 2, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(Constants.REMINDER_UPCOMING_KEY, upcomingList, 2, TimeUnit.HOURS);
    }

    private List<ReminderVO> calculateReminders() {
        List<ReminderVO> result = new ArrayList<>();
        List<MaintenanceStandard> standards = standardMapper.selectList(
                new LambdaQueryWrapper<MaintenanceStandard>().eq(MaintenanceStandard::getEnabled, 1));
        for (MaintenanceStandard standard : standards) {
            Equipment equipment = equipmentMapper.selectById(standard.getEquipmentId());
            if (equipment == null) continue;

            ReminderVO reminder = new ReminderVO();
            reminder.setEquipmentId(equipment.getId());
            reminder.setEquipmentName(equipment.getName());
            reminder.setEquipmentCode(equipment.getCode());
            reminder.setStandardId(standard.getId());
            reminder.setItemName(standard.getItemName());
            reminder.setTriggerType(standard.getTriggerType());
            reminder.setCurrentRuntime(equipment.getCurrentRuntime());

            if (TriggerType.CALENDAR.equals(standard.getTriggerType())) {
                LocalDate lastDate = equipment.getLastMaintenanceDate();
                if (lastDate == null) lastDate = equipment.getInstallDate();
                if (lastDate == null) continue;
                LocalDate nextDate = lastDate.plusDays(standard.getCycleDays());
                reminder.setNextDate(nextDate);
                long days = ChronoUnit.DAYS.between(LocalDate.now(), nextDate);
                if (days < 0) {
                    reminder.setOverdueDays((int) Math.abs(days));
                    reminder.setDaysRemaining(0);
                } else {
                    reminder.setDaysRemaining((int) days);
                    reminder.setOverdueDays(0);
                }
            } else if (TriggerType.RUNTIME.equals(standard.getTriggerType())) {
                BigDecimal targetRuntime = equipment.getLastMaintenanceRuntime()
                        .add(standard.getCycleHours());
                reminder.setTargetRuntime(targetRuntime);
                if (equipment.getCurrentRuntime() != null && equipment.getCurrentRuntime().compareTo(targetRuntime) >= 0) {
                    reminder.setOverdueDays(1);
                    reminder.setDaysRemaining(0);
                } else if (equipment.getCurrentRuntime() != null) {
                    BigDecimal remaining = targetRuntime.subtract(equipment.getCurrentRuntime());
                    reminder.setDaysRemaining(remaining.intValue());
                    reminder.setOverdueDays(0);
                }
            }
            result.add(reminder);
        }
        return result;
    }

    private String calculateEquipmentMaintenanceStatus(Equipment equipment) {
        List<MaintenanceStandard> standards = standardMapper.selectList(
                new LambdaQueryWrapper<MaintenanceStandard>()
                        .eq(MaintenanceStandard::getEquipmentId, equipment.getId())
                        .eq(MaintenanceStandard::getEnabled, 1));
        for (MaintenanceStandard standard : standards) {
            if (TriggerType.CALENDAR.equals(standard.getTriggerType()) && standard.getCycleDays() != null) {
                LocalDate lastDate = equipment.getLastMaintenanceDate();
                if (lastDate == null) lastDate = equipment.getInstallDate();
                if (lastDate == null) continue;
                LocalDate nextDate = lastDate.plusDays(standard.getCycleDays());
                long days = ChronoUnit.DAYS.between(LocalDate.now(), nextDate);
                if (days < 0) return "OVERDUE";
                if (days <= (standard.getRemindDaysBefore() != null ? standard.getRemindDaysBefore() : 3)) {
                    return "UPCOMING";
                }
            }
        }
        return "NORMAL";
    }
}
