package com.pm.scheduler.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthlyTrendVO {

    private String month;
    private int totalTasks;
    private int completedTasks;
    private BigDecimal completionRate;
}
