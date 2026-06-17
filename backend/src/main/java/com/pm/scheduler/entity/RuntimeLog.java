package com.pm.scheduler.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("runtime_log")
public class RuntimeLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long equipmentId;

    private BigDecimal runtimeValue;

    private BigDecimal incrementValue;

    private Long operatorId;

    private LocalDateTime recordedAt;

    private LocalDateTime createdAt;
}
