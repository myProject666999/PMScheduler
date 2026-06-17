package com.pm.scheduler.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EquipmentCreateDTO {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String model;

    private String productionLine;

    private LocalDate installDate;

    private String status;

    private BigDecimal currentRuntime;
}
