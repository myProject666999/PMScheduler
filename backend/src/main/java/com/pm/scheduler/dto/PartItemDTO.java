package com.pm.scheduler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PartItemDTO {

    @NotBlank
    private String partName;

    private String partCode;

    @NotNull
    private BigDecimal quantity;

    private String unit;
}
