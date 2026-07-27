package com.periodtracker.repository;

import com.periodtracker.entity.PeriodLog;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeriodLogRepository extends JpaRepository<PeriodLog, Long> {

    List<PeriodLog> findByUserIdOrderByStartDateDesc(Long userId);

    List<PeriodLog> findByUserIdAndStartDateBetweenOrderByStartDateDesc(
            Long userId, LocalDate from, LocalDate to);

    List<PeriodLog> findByUserIdAndStartDateBeforeOrderByStartDateDesc(
            Long userId, LocalDate cursor);

    Optional<PeriodLog> findByUserIdAndStartDate(Long userId, LocalDate startDate);

    List<PeriodLog> findByUserIdOrderByStartDateAsc(Long userId);
}
