package com.pm.scheduler.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pm.scheduler.common.constant.WorkOrderStatus;
import com.pm.scheduler.entity.Equipment;
import com.pm.scheduler.entity.SysUser;
import com.pm.scheduler.entity.WorkOrder;
import com.pm.scheduler.mapper.EquipmentMapper;
import com.pm.scheduler.mapper.SysUserMapper;
import com.pm.scheduler.mapper.WorkOrderMapper;
import com.pm.scheduler.service.StatisticsService;
import com.pm.scheduler.vo.CompletionStatVO;
import com.pm.scheduler.vo.MonthlyTrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final WorkOrderMapper workOrderMapper;
    private final SysUserMapper sysUserMapper;
    private final EquipmentMapper equipmentMapper;

    @Override
    public List<CompletionStatVO> completionByPerson(Integer year, Integer month) {
        LocalDateTime startTime = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime endTime = LocalDate.of(year, month, YearMonth.of(year, month).lengthOfMonth()).atTime(23, 59, 59);

        List<WorkOrder> orders = workOrderMapper.selectList(
                new LambdaQueryWrapper<WorkOrder>()
                        .ge(WorkOrder::getCreatedAt, startTime)
                        .le(WorkOrder::getCreatedAt, endTime)
                        .isNotNull(WorkOrder::getExecuteUserId));

        Map<Long, List<WorkOrder>> grouped = orders.stream()
                .collect(Collectors.groupingBy(WorkOrder::getExecuteUserId));

        List<CompletionStatVO> result = new ArrayList<>();
        for (Map.Entry<Long, List<WorkOrder>> entry : grouped.entrySet()) {
            CompletionStatVO stat = new CompletionStatVO();
            stat.setId(entry.getKey());
            SysUser user = sysUserMapper.selectById(entry.getKey());
            stat.setName(user != null ? user.getRealName() : "未知");
            int total = entry.getValue().size();
            int completed = (int) entry.getValue().stream()
                    .filter(o -> WorkOrderStatus.COMPLETED.equals(o.getStatus())).count();
            int overdue = (int) entry.getValue().stream()
                    .filter(o -> WorkOrderStatus.COMPLETED.equals(o.getStatus())
                            && o.getCompleteAt() != null && o.getPlanDate() != null
                            && o.getCompleteAt().toLocalDate().isAfter(o.getPlanDate()))
                    .count();
            stat.setTotalTasks(total);
            stat.setCompletedTasks(completed);
            stat.setOverdueTasks(overdue);
            stat.setCompletionRate(total > 0 ? new BigDecimal(completed)
                    .divide(new BigDecimal(total), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            stat.setOverdueRate(total > 0 ? new BigDecimal(overdue)
                    .divide(new BigDecimal(total), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            result.add(stat);
        }
        return result;
    }

    @Override
    public List<CompletionStatVO> completionByEquipment(Integer year, Integer month) {
        LocalDateTime startTime = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime endTime = LocalDate.of(year, month, YearMonth.of(year, month).lengthOfMonth()).atTime(23, 59, 59);

        List<WorkOrder> orders = workOrderMapper.selectList(
                new LambdaQueryWrapper<WorkOrder>()
                        .ge(WorkOrder::getCreatedAt, startTime)
                        .le(WorkOrder::getCreatedAt, endTime));

        Map<Long, List<WorkOrder>> grouped = orders.stream()
                .collect(Collectors.groupingBy(WorkOrder::getEquipmentId));

        List<CompletionStatVO> result = new ArrayList<>();
        for (Map.Entry<Long, List<WorkOrder>> entry : grouped.entrySet()) {
            CompletionStatVO stat = new CompletionStatVO();
            stat.setId(entry.getKey());
            Equipment equipment = equipmentMapper.selectById(entry.getKey());
            stat.setName(equipment != null ? equipment.getName() : "未知");
            int total = entry.getValue().size();
            int completed = (int) entry.getValue().stream()
                    .filter(o -> WorkOrderStatus.COMPLETED.equals(o.getStatus())).count();
            int overdue = (int) entry.getValue().stream()
                    .filter(o -> WorkOrderStatus.COMPLETED.equals(o.getStatus())
                            && o.getCompleteAt() != null && o.getPlanDate() != null
                            && o.getCompleteAt().toLocalDate().isAfter(o.getPlanDate()))
                    .count();
            stat.setTotalTasks(total);
            stat.setCompletedTasks(completed);
            stat.setOverdueTasks(overdue);
            stat.setCompletionRate(total > 0 ? new BigDecimal(completed)
                    .divide(new BigDecimal(total), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            stat.setOverdueRate(total > 0 ? new BigDecimal(overdue)
                    .divide(new BigDecimal(total), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            result.add(stat);
        }
        return result;
    }

    @Override
    public List<MonthlyTrendVO> monthlyTrend() {
        List<MonthlyTrendVO> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate now = LocalDate.now();

        for (int i = 11; i >= 0; i--) {
            YearMonth ym = YearMonth.from(now.minusMonths(i));
            LocalDateTime startTime = ym.atDay(1).atStartOfDay();
            LocalDateTime endTime = ym.atEndOfMonth().atTime(23, 59, 59);

            List<WorkOrder> orders = workOrderMapper.selectList(
                    new LambdaQueryWrapper<WorkOrder>()
                            .ge(WorkOrder::getCreatedAt, startTime)
                            .le(WorkOrder::getCreatedAt, endTime));

            MonthlyTrendVO trend = new MonthlyTrendVO();
            trend.setMonth(ym.format(formatter));
            int total = orders.size();
            int completed = (int) orders.stream()
                    .filter(o -> WorkOrderStatus.COMPLETED.equals(o.getStatus())).count();
            trend.setTotalTasks(total);
            trend.setCompletedTasks(completed);
            trend.setCompletionRate(total > 0 ? new BigDecimal(completed)
                    .divide(new BigDecimal(total), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            result.add(trend);
        }
        return result;
    }
}
