package com.periodtracker.service;

import com.periodtracker.config.PredictionProperties;
import com.periodtracker.dto.PredictionResponse;
import com.periodtracker.entity.UserProfile;
import com.periodtracker.exception.NotFoundException;
import com.periodtracker.prediction.CyclePredictor;
import com.periodtracker.prediction.OnboardingRequiredException;
import com.periodtracker.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PredictionService {

    private final CyclePredictor predictor;
    private final CycleStatisticsService statsService;
    private final UserRepository userRepository;

    public PredictionService(PredictionProperties props,
                             CycleStatisticsService statsService,
                             UserRepository userRepository) {
        this.predictor = new CyclePredictor(props);
        this.statsService = statsService;
        this.userRepository = userRepository;
    }

    public PredictionResponse predict(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found with id: " + userId);
        }

        UserProfile profile = statsService.getProfile(userId)
                .orElseThrow(() -> new OnboardingRequiredException(userId));

        if (!profile.isOnboardingCompleted()) {
            throw new OnboardingRequiredException(userId);
        }

        List<LocalDate> periodStartDates = statsService.getPeriodStartDates(userId);
        CyclePredictor.Prediction p = predictor.predict(
                periodStartDates,
                profile.getTypicalCycleLengthDays(),
                profile.getTypicalPeriodDurationDays(),
                profile.getLastPeriodStartDate()
        );

        PredictionResponse.NextPeriod nextPeriod = new PredictionResponse.NextPeriod(
                p.predictedNextStart(),
                new PredictionResponse.ConfidenceBand(p.earliest(), p.latest()),
                p.confidenceLevel()
        );

        PredictionResponse.Ovulation ovulation = new PredictionResponse.Ovulation(
                p.predictedOvulation(),
                new PredictionResponse.DateRange(p.fertileStart(), p.fertileEnd())
        );

        PredictionResponse.Explanation explanation = new PredictionResponse.Explanation(
                p.method(),
                p.sampleSize(),
                p.avgCycleLength(),
                p.cycleLengthStdDev(),
                p.onboardingBaselineDays(),
                p.onboardingPeriodDurationDays(),
                p.dataSource(),
                p.confidenceNote(),
                p.lutealPhaseDays(),
                p.lastPeriodStart(),
                p.excludedCycles()
        );

        return new PredictionResponse(nextPeriod, ovulation, explanation, Instant.now());
    }
}
