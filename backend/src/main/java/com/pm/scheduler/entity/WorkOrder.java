package com.pm.scheduler.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("work_order")
public class WorkOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long equipmentId;

    private Long standardId;

    private String status = "PENDING";

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
}
