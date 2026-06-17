package com.pm.scheduler.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CompletionStatVO {

    private Long id;
    private String name;
    private int totalTasks;
    private int completedTasks;
    private int overdueTasks;
    private BigDecimal completionRate;
    private BigDecimal overdueRate;
}
