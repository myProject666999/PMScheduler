package com.pm.scheduler.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class WorkOrderQueryDTO {

    private String status;
    private Long equipmentId;
    private Long executeUserId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String keyword;
    private int pageNum = 1;
    private int pageSize = 10;
}
