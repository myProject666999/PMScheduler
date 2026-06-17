package com.pm.scheduler.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RuntimeLogVO {

    private Long id;
    private Long equipmentId;
    private BigDecimal runtimeValue;
    private BigDecimal incrementValue;
    private Long operatorId;
    private LocalDateTime recordedAt;
    private LocalDateTime createdAt;

    private String operatorName;
}
