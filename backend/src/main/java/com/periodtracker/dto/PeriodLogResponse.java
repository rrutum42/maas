package com.periodtracker.dto;

import java.time.LocalDate;

public record PeriodLogResponse(
        Long id,
        LocalDate startDate,
        LocalDate endDate,
        Integer flowIntensity,
        String notes,
        Integer cycleLengthDays) {
}
