package com.periodtracker.service;

import com.periodtracker.entity.PeriodLog;
import com.periodtracker.entity.UserCycleStats;
import com.periodtracker.entity.UserProfile;
import com.periodtracker.repository.PeriodLogRepository;
import com.periodtracker.repository.UserCycleStatsRepository;
import com.periodtracker.repository.UserProfileRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CycleStatisticsService {

    private final PeriodLogRepository periodLogRepository;
    private final UserCycleStatsRepository statsRepository;
    private final UserProfileRepository userProfileRepository;

    public CycleStatisticsService(PeriodLogRepository periodLogRepository,
                                  UserCycleStatsRepository statsRepository,
                                  UserProfileRepository userProfileRepository) {
        this.periodLogRepository = periodLogRepository;
        this.statsRepository = statsRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public List<LocalDate> getPeriodStartDates(Long userId) {
        return periodLogRepository.findByUserIdOrderByStartDateAsc(userId).stream()
                .map(PeriodLog::getStartDate)
                .toList();
    }

    public Optional<UserCycleStats> getStats(Long userId) {
        return statsRepository.findById(userId);
    }

    public Optional<UserProfile> getProfile(Long userId) {
        return userProfileRepository.findById(userId);
    }
}
