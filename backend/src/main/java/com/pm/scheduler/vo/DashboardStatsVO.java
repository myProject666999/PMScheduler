package com.pm.scheduler.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardStatsVO {

    private int totalEquipment;
    private int normalCount;
    private int upcomingCount;
    private int overdueCount;
    private int maintenanceCount;
    private int pendingOrders;
    private int dispatchedOrders;
    private int executingOrders;
    private int reviewingOrders;
    private BigDecimal monthlyCompletionRate;
}
