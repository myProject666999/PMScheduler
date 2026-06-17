package com.pm.scheduler.vo;

import lombok.Data;

import java.util.List;

@Data
public class EquipmentDetailVO extends EquipmentVO {

    private List<MaintenanceStandardVO> standards;
    private List<WorkOrderVO> recentOrders;
    private List<RuntimeLogVO> runtimeLogs;
}
