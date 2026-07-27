package com.periodtracker.repository;

import com.periodtracker.entity.UserCycleStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCycleStatsRepository extends JpaRepository<UserCycleStats, Long> {
}
