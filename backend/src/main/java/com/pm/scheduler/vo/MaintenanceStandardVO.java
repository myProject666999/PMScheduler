package com.pm.scheduler.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MaintenanceStandardVO {

    private Long id;
    private Long equipmentId;
    private String itemName;
    private String triggerType;
    private Integer cycleDays;
    private BigDecimal cycleHours;
    private Integer remindDaysBefore;
    private BigDecimal remindHoursBefore;
    private String content;
    private Integer enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String equipmentName;
}
