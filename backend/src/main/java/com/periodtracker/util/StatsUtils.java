package com.periodtracker.util;

import java.util.Collection;

public class StatsUtils {

    public static double mean(Collection<? extends Number> values) {
        return values.stream()
                .mapToDouble(Number::doubleValue)
                .average()
                .orElse(0);
    }

    public static double stdDev(Collection<? extends Number> values) {
        double m = mean(values);
        double sumSq = values.stream()
                .mapToDouble(v -> Math.pow(v.doubleValue() - m, 2))
                .sum();
        return Math.sqrt(sumSq / values.size());
    }
}
