package com.pm.scheduler.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EquipmentUpdateDTO {

    private String name;
    private String model;
    private String productionLine;
    private LocalDate installDate;
    private String status;
}
