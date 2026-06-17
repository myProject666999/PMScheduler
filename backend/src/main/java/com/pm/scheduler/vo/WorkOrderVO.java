package com.pm.scheduler.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkOrderVO {

    private Long id;
    private String orderNo;
    private Long equipmentId;
    private Long standardId;
    private String status;
    private Long dispatchUserId;
    private Long executeUserId;
    private LocalDate planDate;
    private LocalDate actualDate;
    private BigDecimal actualHours;
    private String maintenanceContent;
    private String triggerType;
    private LocalDateTime dispatchAt;
    private LocalDateTime executeAt;
    private LocalDateTime completeAt;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String equipmentName;
    private String equipmentCode;
    private String standardItemName;
    private String executeUserName;
    private String dispatchUserName;
    private List<WorkOrderPartVO> parts;
}
