package com.pm.scheduler.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("equipment")
public class Equipment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private String model;

    private String productionLine;

    private LocalDate installDate;

    @TableField("status")
    private String status = "NORMAL";

    private BigDecimal currentRuntime;

    private BigDecimal lastMaintenanceRuntime;

    private LocalDate lastMaintenanceDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
