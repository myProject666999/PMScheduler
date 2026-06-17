package com.pm.scheduler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkOrderReviewDTO {

    @NotNull
    private Boolean approved;

    private String remark;
}
