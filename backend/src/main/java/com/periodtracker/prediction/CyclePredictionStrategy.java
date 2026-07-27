package com.periodtracker.prediction;

import java.time.LocalDate;
import java.util.List;

public interface CyclePredictionStrategy {

    Prediction predict(List<LocalDate> periodStartDates,
                       int typicalCycleLengthDays,
                       int typicalPeriodDurationDays,
                       LocalDate lastPeriodStartDate);
}
