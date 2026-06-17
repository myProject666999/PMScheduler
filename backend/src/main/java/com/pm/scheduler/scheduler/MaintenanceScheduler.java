package com.pm.scheduler.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pm.scheduler.common.constant.TriggerType;
import com.pm.scheduler.entity.Equipment;
import com.pm.scheduler.entity.MaintenanceStandard;
import com.pm.scheduler.mapper.EquipmentMapper;
import com.pm.scheduler.mapper.MaintenanceStandardMapper;
import com.pm.scheduler.service.DashboardService;
import com.pm.scheduler.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class MaintenanceScheduler {

    private final MaintenanceStandardMapper standardMapper;
    private final EquipmentMapper equipmentMapper;
    private final WorkOrderService workOrderService;
    private final DashboardService dashboardService;

    @Scheduled(cron = "0 0 * * * ?")
    public void scanCalendarTriggers() {
        log.info("开始扫描日历触发保养标准...");
        List<MaintenanceStandard> standards = standardMapper.selectList(
                new LambdaQueryWrapper<MaintenanceStandard>()
                        .eq(MaintenanceStandard::getTriggerType, TriggerType.CALENDAR)
                        .eq(MaintenanceStandard::getEnabled, 1));
        for (MaintenanceStandard standard : standards) {
            Equipment equipment = equipmentMapper.selectById(standard.getEquipmentId());
            if (equipment == null) continue;
            LocalDate lastDate = equipment.getLastMaintenanceDate();
            if (lastDate == null) lastDate = equipment.getInstallDate();
            if (lastDate == null) continue;
            LocalDate nextDate = lastDate.plusDays(standard.getCycleDays());
            LocalDate remindDate = nextDate.minusDays(standard.getRemindDaysBefore());
            if (!LocalDate.now().isBefore(remindDate)) {
                workOrderService.autoGenerate(equipment.getId(), standard.getId(), TriggerType.CALENDAR);
            }
        }
        dashboardService.refreshReminders();
        log.info("日历触发扫描完成");
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void scanRuntimeTriggers() {
        log.info("开始扫描运行时数触发保养标准...");
        List<MaintenanceStandard> standards = standardMapper.selectList(
                new LambdaQueryWrapper<MaintenanceStandard>()
                        .eq(MaintenanceStandard::getTriggerType, TriggerType.RUNTIME)
                        .eq(MaintenanceStandard::getEnabled, 1));
        for (MaintenanceStandard standard : standards) {
            Equipment equipment = equipmentMapper.selectById(standard.getEquipmentId());
            if (equipment == null) continue;
            if (equipment.getCurrentRuntime() == null) continue;
            BigDecimal targetRuntime = equipment.getLastMaintenanceRuntime().add(standard.getCycleHours());
            BigDecimal remindRuntime = targetRuntime.subtract(standard.getRemindHoursBefore());
            if (equipment.getCurrentRuntime().compareTo(remindRuntime) >= 0) {
                workOrderService.autoGenerate(equipment.getId(), standard.getId(), TriggerType.RUNTIME);
            }
        }
        dashboardService.refreshReminders();
        log.info("运行时数触发扫描完成");
    }
}
