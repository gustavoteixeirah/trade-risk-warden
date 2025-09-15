package com.teixeirah.trw.infra.primary.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public abstract class Dtos {
    public enum ThresholdTypeDto {ABSOLUTE, PERCENTAGE}

    public record ThresholdDto(@NotNull ThresholdTypeDto type, @NotNull @DecimalMin("0") BigDecimal value) {
    }

    public record RegisterUserRequest(
            @NotBlank String clientId,
            @NotBlank String apiKey,
            @NotBlank String apiSecret,
            @NotNull ThresholdDto maxRisk,
            @NotNull ThresholdDto dailyRisk,
            @NotBlank String currency,
            String tz
    ) {
    }

}


