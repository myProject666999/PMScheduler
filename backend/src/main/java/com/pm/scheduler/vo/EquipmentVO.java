package com.pm.scheduler.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EquipmentVO {

    private Long id;
    private String code;
    private String name;
    private String model;
    private String productionLine;
    private LocalDate installDate;
    private String status;
    private BigDecimal currentRuntime;
    private BigDecimal lastMaintenanceRuntime;
    private LocalDate lastMaintenanceDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private LocalDate nextMaintenanceDate;
    private Integer daysUntilNext;
    private String maintenanceStatus;
}
