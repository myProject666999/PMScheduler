package com.pm.scheduler.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StandardUpdateDTO {

    private String itemName;
    private String triggerType;
    private Integer cycleDays;
    private BigDecimal cycleHours;
    private Integer remindDaysBefore;
    private BigDecimal remindHoursBefore;
    private String content;
    private Integer enabled;
}
