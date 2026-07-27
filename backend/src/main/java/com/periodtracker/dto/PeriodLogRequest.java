package com.periodtracker.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PeriodLogRequest(
        @NotNull Long userId,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        @Min(1) @Max(5) Integer flowIntensity,
        String notes) {
}
