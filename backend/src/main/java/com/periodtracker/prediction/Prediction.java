package com.periodtracker.prediction;

import java.time.LocalDate;

public record Prediction(
        LocalDate predictedNextStart,
        LocalDate earliest,
        LocalDate latest,
        double confidenceLevel,
        LocalDate predictedOvulation,
        LocalDate fertileStart,
        LocalDate fertileEnd,
        String method,
        int sampleSize,
        double avgCycleLength,
        double cycleLengthStdDev,
        int onboardingBaselineDays,
        int onboardingPeriodDurationDays,
        String dataSource,
        String confidenceNote,
        int lutealPhaseDays,
        LocalDate lastPeriodStart) {
}
