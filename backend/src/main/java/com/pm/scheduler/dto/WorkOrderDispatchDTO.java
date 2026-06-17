package com.pm.scheduler.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WorkOrderDispatchDTO {

    @NotNull
    private Long executeUserId;

    @NotNull
    private LocalDate planDate;
}
