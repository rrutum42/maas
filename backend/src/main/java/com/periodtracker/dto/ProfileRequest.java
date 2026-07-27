package com.periodtracker.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ProfileRequest(
        @NotNull Long userId,
        @Min(21) @Max(45) int typicalCycleLengthDays,
        @Min(2) @Max(10) int typicalPeriodDurationDays,
        @NotNull LocalDate lastPeriodStartDate) {
}
