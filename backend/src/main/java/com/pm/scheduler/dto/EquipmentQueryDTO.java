package com.pm.scheduler.dto;

import lombok.Data;

@Data
public class EquipmentQueryDTO {

    private String keyword;
    private String productionLine;
    private String status;
    private int pageNum = 1;
    private int pageSize = 10;
}
