package com.pm.scheduler.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("maintenance_standard")
public class MaintenanceStandard {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long equipmentId;

    private String itemName;

    private String triggerType;

    private Integer cycleDays;

    private BigDecimal cycleHours;

    private Integer remindDaysBefore = 3;

    private BigDecimal remindHoursBefore = new BigDecimal("50");

    private String content;

    private Integer enabled = 1;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
