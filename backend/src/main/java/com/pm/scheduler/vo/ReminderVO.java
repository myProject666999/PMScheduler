package com.pm.scheduler.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReminderVO {

    private Long equipmentId;
    private String equipmentName;
    private String equipmentCode;
    private Long standardId;
    private String itemName;
    private String triggerType;
    private Integer daysRemaining;
    private Integer overdueDays;
    private LocalDate nextDate;
    private BigDecimal currentRuntime;
    private BigDecimal targetRuntime;
}
