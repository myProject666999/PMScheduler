package com.pm.scheduler.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("work_order_part")
public class WorkOrderPart {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long workOrderId;

    private String partName;

    private String partCode;

    private BigDecimal quantity;

    private String unit;

    private LocalDateTime createdAt;
}
