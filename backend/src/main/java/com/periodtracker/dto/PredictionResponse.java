package com.periodtracker.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PredictionResponse(
        NextPeriod nextPeriod,
        Ovulation ovulation,
        Explanation explanation,
        Instant computedAt) {

    public record NextPeriod(
            LocalDate predictedStartDate,
            ConfidenceBand confidenceBand,
            double confidenceLevel) {
    }

    public record Ovulation(
            LocalDate predictedDate,
            DateRange fertileWindow) {
    }

    public record ConfidenceBand(LocalDate earliest, LocalDate latest) {
    }

    public record DateRange(LocalDate start, LocalDate end) {
    }

    public record Explanation(
            String method,
            int sampleSize,
            double avgCycleLengthDays,
            Double cycleLengthStdDev,
            int onboardingBaselineDays,
            int onboardingPeriodDurationDays,
            String dataSource,
            String confidenceNote,
            int lutealPhaseDays,
            LocalDate lastPeriodStart,
            List<Integer> excludedCycles) {
    }
}
