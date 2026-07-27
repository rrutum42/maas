package com.periodtracker.prediction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.periodtracker.config.PredictionProperties;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CyclePredictorTest {

    private CyclePredictor predictor;

    @BeforeEach
    void setUp() {
        PredictionProperties props = new PredictionProperties();
        props.setWindowSize(6);
        props.setObservedMinSample(2);
        props.setOnboardingSigma(4);
        props.setLutealPhaseDays(14);
        predictor = new CyclePredictor(props);
    }

    @Test
    void zeroLogsUsesOnboardingBaseline() {
        Prediction p = predictor.predict(
                List.of(), 28, 5, LocalDate.of(2026, 1, 5));
        assertEquals(LocalDate.of(2026, 2, 2), p.predictedNextStart());
        assertEquals("onboarding_baseline", p.dataSource());
        assertEquals(0, p.sampleSize());
    }

    @Test
    void oneLogStillUsesOnboardingBaseline() {
        Prediction p = predictor.predict(
                List.of(LocalDate.of(2026, 1, 5)), 28, 5, LocalDate.of(2026, 1, 5));
        assertEquals(LocalDate.of(2026, 2, 2), p.predictedNextStart());
        assertEquals("onboarding_baseline", p.dataSource());
        assertEquals(1, p.sampleSize());
    }

    @Test
    void regularCyclesProduceObservedStats() {
        Prediction p = predictor.predict(
                List.of(
                        LocalDate.of(2026, 1, 5),
                        LocalDate.of(2026, 2, 2),
                        LocalDate.of(2026, 3, 2)),
                28, 5, LocalDate.of(2026, 1, 5));
        assertEquals("observed", p.dataSource());
        assertEquals("rolling_average_with_variance_band", p.method());
        assertTrue(p.sampleSize() >= 2);
        assertEquals(LocalDate.of(2026, 3, 30), p.predictedNextStart());
    }

    @Test
    void irregularCyclesProduceWiderBand() {
        Prediction p = predictor.predict(
                List.of(
                        LocalDate.of(2026, 1, 5),
                        LocalDate.of(2026, 2, 9),
                        LocalDate.of(2026, 3, 10)),
                28, 5, LocalDate.of(2026, 1, 5));
        assertTrue(p.cycleLengthStdDev() >= 2.0);
    }

    @Test
    void ovulationOffsetUsesLutealPhase() {
        Prediction p = predictor.predict(
                List.of(
                        LocalDate.of(2026, 1, 5),
                        LocalDate.of(2026, 2, 2),
                        LocalDate.of(2026, 3, 2)),
                28, 5, LocalDate.of(2026, 1, 5));
        assertEquals(14, p.lutealPhaseDays());
        assertEquals(p.predictedNextStart().minusDays(14), p.predictedOvulation());
    }
}
