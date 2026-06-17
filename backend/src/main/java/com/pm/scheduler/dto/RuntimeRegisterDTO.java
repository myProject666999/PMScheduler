package com.pm.scheduler.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RuntimeRegisterDTO {

    @NotNull
    private BigDecimal runtimeValue;

    private LocalDateTime recordedAt;
}
