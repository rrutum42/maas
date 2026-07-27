package com.periodtracker.service;

import com.periodtracker.dto.PeriodListResponse;
import com.periodtracker.dto.PeriodLogRequest;
import com.periodtracker.dto.PeriodLogResponse;
import com.periodtracker.entity.PeriodLog;
import com.periodtracker.entity.UserCycleStats;
import com.periodtracker.exception.NotFoundException;
import com.periodtracker.exception.ValidationException;
import com.periodtracker.repository.PeriodLogRepository;
import com.periodtracker.util.StatsUtils;
import com.periodtracker.repository.UserCycleStatsRepository;
import com.periodtracker.repository.UserProfileRepository;
import com.periodtracker.repository.UserRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PeriodService {

    private final PeriodLogRepository periodLogRepository;
    private final UserRepository userRepository;
    private final UserCycleStatsRepository statsRepository;
    private final UserProfileRepository profileRepository;

    public PeriodService(PeriodLogRepository periodLogRepository,
                         UserRepository userRepository,
                         UserCycleStatsRepository statsRepository,
                         UserProfileRepository profileRepository) {
        this.periodLogRepository = periodLogRepository;
        this.userRepository = userRepository;
        this.statsRepository = statsRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional
    public PeriodLogResult logPeriod(PeriodLogRequest request) {
        // Validate user exists
        if (!userRepository.existsById(request.userId())) {
            throw new NotFoundException("User not found with id: " + request.userId());
        }
        // Validate dates are sensible
        if (request.startDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Start date cannot be in the future");
        }
        if (request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw new ValidationException("End date must be on or after start date");
        }

        PeriodLog log = new PeriodLog();
        log.setUserId(request.userId());
        log.setStartDate(request.startDate());

        // Default end date from user's profile if not provided
        LocalDate endDate = request.endDate();
        if (endDate == null) {
            endDate = profileRepository.findById(request.userId())
                    .map(p -> request.startDate().plusDays(p.getTypicalPeriodDurationDays()))
                    .orElse(request.startDate());
        }
        log.setEndDate(endDate);

        log.setFlowIntensity(request.flowIntensity());
        log.setNotes(request.notes());

        // Insert or detect duplicate (same userId + startDate)
        boolean created;
        try {
            periodLogRepository.save(log);
            created = true;
        } catch (DataIntegrityViolationException e) {
            log = periodLogRepository.findByUserIdAndStartDate(request.userId(), request.startDate())
                    .orElseThrow(() -> e);
            created = false;
        }

        recomputeStats(request.userId());
        return new PeriodLogResult(toResponse(log), created);
    }

    public PeriodListResponse listPeriods(Long userId, LocalDate from, LocalDate to,
                                          LocalDate cursor, int size) {
        // Fetch logs based on cursor pagination, date range, or all
        List<PeriodLog> logs;
        if (cursor != null) {
            logs = periodLogRepository.findByUserIdAndStartDateBeforeOrderByStartDateDesc(
                    userId, cursor);
        } else if (from != null && to != null) {
            logs = periodLogRepository.findByUserIdAndStartDateBetweenOrderByStartDateDesc(
                    userId, from, to);
        } else {
            logs = periodLogRepository.findByUserIdOrderByStartDateDesc(userId);
        }

        // Trim to requested page size and check if more pages exist
        boolean hasMore = logs.size() > size;
        if (hasMore) {
            logs = logs.subList(0, size);
        }

        // Enrich each log with its cycle length (days since previous period start)
        List<PeriodLog> allAsc = periodLogRepository.findByUserIdOrderByStartDateAsc(userId);
        List<PeriodLogResponse> data = new ArrayList<>();
        for (PeriodLog log : logs) {
            Integer cycleLength = findCycleLength(log, allAsc);
            data.add(new PeriodLogResponse(
                    log.getId(), log.getStartDate(), log.getEndDate(),
                    log.getFlowIntensity(), log.getNotes(), cycleLength));
        }

        String nextCursor = hasMore ? logs.get(logs.size() - 1).getStartDate().toString() : null;
        return new PeriodListResponse(data, new PeriodListResponse.Pagination(nextCursor, hasMore));
    }

    // Number of days from the previous period start to this log's start
    private Integer findCycleLength(PeriodLog log, List<PeriodLog> allAsc) {
        int idx = findIndex(allAsc, log.getStartDate());
        if (idx > 0) {
            long days = ChronoUnit.DAYS.between(allAsc.get(idx - 1).getStartDate(), log.getStartDate());
            return (int) days;
        }
        return null; // first recorded period — no previous cycle to compare
    }

    // Binary search since the list is sorted by start date ascending
    private int findIndex(List<PeriodLog> logs, LocalDate startDate) {
        int lo = 0, hi = logs.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            int cmp = logs.get(mid).getStartDate().compareTo(startDate);
            if (cmp < 0) lo = mid + 1;
            else if (cmp > 0) hi = mid - 1;
            else return mid;
        }
        return -1;
    }

    // Recalculate aggregate cycle stats and persist them
    private void recomputeStats(Long userId) {
        List<PeriodLog> logs = periodLogRepository.findByUserIdOrderByStartDateAsc(userId);
        if (logs.isEmpty()) return;

        // Compute cycle lengths (days from one period start to the next)
        List<Long> cycleLengths = new ArrayList<>();
        for (int i = 1; i < logs.size(); i++) {
            long days = ChronoUnit.DAYS.between(logs.get(i - 1).getStartDate(),
                    logs.get(i).getStartDate());
            cycleLengths.add(days);
        }

        // Compute period durations (days from start to end within each log)
        List<Long> durations = new ArrayList<>();
        for (PeriodLog log : logs) {
            if (log.getEndDate() != null) {
                long days = ChronoUnit.DAYS.between(log.getStartDate(), log.getEndDate());
                durations.add(days);
            }
        }

        // Fetch existing stats or create a new record for this user
        UserCycleStats stats = statsRepository.findById(userId).orElseGet(() -> {
            UserCycleStats s = new UserCycleStats();
            s.setUserId(userId);
            return s;
        });

        stats.setSampleSize(cycleLengths.size());
        if (!cycleLengths.isEmpty()) {
            stats.setAvgCycleLength(StatsUtils.mean(cycleLengths));
            if (cycleLengths.size() >= 2) {
                stats.setCycleLengthStddev(StatsUtils.stdDev(cycleLengths));
            }
        }
        if (!durations.isEmpty()) {
            stats.setAvgPeriodDuration(StatsUtils.mean(durations));
            if (durations.size() >= 2) {
                stats.setPeriodDurationStddev(StatsUtils.stdDev(durations));
            }
        }
        stats.setLastPeriodStart(logs.get(logs.size() - 1).getStartDate());
        statsRepository.save(stats);
    }

    public PeriodLogResponse toResponse(PeriodLog log) {
        return new PeriodLogResponse(
                log.getId(), log.getStartDate(), log.getEndDate(),
                log.getFlowIntensity(), log.getNotes(), null);
    }

    public record PeriodLogResult(PeriodLogResponse response, boolean created) {}
}
