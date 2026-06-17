package com.pm.scheduler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StandardCreateDTO {

    @NotNull
    private Long equipmentId;

    @NotBlank
    private String itemName;

    @NotBlank
    private String triggerType;

    private Integer cycleDays;

    private BigDecimal cycleHours;

    private Integer remindDaysBefore;

    private BigDecimal remindHoursBefore;

    private String content;
}
