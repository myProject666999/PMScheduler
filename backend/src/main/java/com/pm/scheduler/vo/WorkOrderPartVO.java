package com.pm.scheduler.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WorkOrderPartVO {

    private Long id;
    private Long workOrderId;
    private String partName;
    private String partCode;
    private BigDecimal quantity;
    private String unit;
    private LocalDateTime createdAt;
}
