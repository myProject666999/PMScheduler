package com.pm.scheduler.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class WorkOrderExecuteDTO {

    private BigDecimal actualHours;
    private String maintenanceContent;
    private String remark;
    private List<PartItemDTO> parts;
}
