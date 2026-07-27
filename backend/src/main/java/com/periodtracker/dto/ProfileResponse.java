package com.periodtracker.dto;

import java.time.Instant;
import java.time.LocalDate;

public record ProfileResponse(
        Long userId,
        int typicalCycleLengthDays,
        int typicalPeriodDurationDays,
        LocalDate lastPeriodStartDate,
        boolean onboardingCompleted,
        Instant onboardingCompletedAt) {
}
