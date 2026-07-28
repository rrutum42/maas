package com.periodtracker.prediction;

import com.periodtracker.config.PredictionProperties;
import com.periodtracker.util.StatsUtils;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CyclePredictor implements CyclePredictionStrategy {

    private final int windowSize;          // number of recent cycles used for rolling average
    private final int observedMinSample;   // minimum observed cycles before switching to real data
    private final int onboardingSigma;     // std dev used during onboarding (no data yet)
    private final int lutealPhaseDays;     // days from ovulation to next period

    public CyclePredictor(PredictionProperties props) {
        this.windowSize = props.getWindowSize();
        this.observedMinSample = props.getObservedMinSample();
        this.onboardingSigma = props.getOnboardingSigma();
        this.lutealPhaseDays = props.getLutealPhaseDays();
    }

    /**
     * Predict the next period start, ovulation window, and fertile window.
     * Uses onboarding defaults when insufficient data exists; otherwise
     * computes a rolling average over recent observed cycles.
     */
    public Prediction predict(List<LocalDate> periodStartDates,
                              int typicalCycleLengthDays,
                              int typicalPeriodDurationDays,
                              LocalDate lastPeriodStartDate) {
        int sampleSize = periodStartDates.size();
        LocalDate anchorDate = sampleSize > 0
                ? periodStartDates.get(sampleSize - 1)
                : lastPeriodStartDate;

        double avgCycleLength;
        double stdDev;
        String method;
        String dataSource;
        String confidenceNote;

        // Fall back to onboarding defaults until we have enough real data
        if (sampleSize < observedMinSample) {
            avgCycleLength = typicalCycleLengthDays;
            stdDev = onboardingSigma;
            method = "onboarding_baseline";
            dataSource = "onboarding_baseline";
            confidenceNote = sampleSize == 0
                    ? "Based on your onboarding info. Log your first period to get started."
                    : "Based on your onboarding info. Log more periods to improve accuracy.";
        } else {
            // Collect all observed cycle lengths from consecutive start dates
            List<Double> cycleLengths = new ArrayList<>();
            for (int i = 1; i < periodStartDates.size(); i++) {
                long days = ChronoUnit.DAYS.between(
                        periodStartDates.get(i - 1), periodStartDates.get(i));
                cycleLengths.add((double) days);
            }

            // Rolling window over the most recent cycles
            int effectiveSampleSize = cycleLengths.size();
            int start = Math.max(0, effectiveSampleSize - windowSize);
            List<Double> window = cycleLengths.subList(start, effectiveSampleSize);

            avgCycleLength = StatsUtils.mean(window);
            stdDev = Math.max(StatsUtils.stdDev(window), 2.0);
            method = "rolling_average_with_variance_band";
            dataSource = "observed";
            confidenceNote = "Based on your last " + window.size() + " observed cycles.";
        }

        // Compute prediction range (next period start ± std dev)
        int avgRounded = (int) Math.round(avgCycleLength);
        LocalDate predictedNextStart = anchorDate.plusDays(avgRounded);
        LocalDate earliest = anchorDate.plusDays((int) Math.round(avgCycleLength - stdDev));
        LocalDate latest = anchorDate.plusDays((int) Math.round(avgCycleLength + stdDev));

        // Ovulation is estimated as luteal phase length before next period
        LocalDate predictedOvulation = predictedNextStart.minusDays(lutealPhaseDays);
        int fertileSpread = stdDev > 2 ? (int) Math.ceil(stdDev) - 1 : 0;
        LocalDate fertileStart = predictedOvulation.minusDays(2 + fertileSpread);
        LocalDate fertileEnd = predictedOvulation.plusDays(1 + fertileSpread);

        return new Prediction(
                predictedNextStart,
                earliest,
                latest,
                predictedOvulation,
                fertileStart,
                fertileEnd,
                method,
                sampleSize,
                avgCycleLength,
                stdDev,
                typicalCycleLengthDays,
                typicalPeriodDurationDays,
                dataSource,
                confidenceNote,
                lutealPhaseDays,
                anchorDate
        );
    }
}
