package com.periodtracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "typical_cycle_length_days", nullable = false)
    private int typicalCycleLengthDays;

    @Column(name = "typical_period_duration_days", nullable = false)
    private int typicalPeriodDurationDays;

    @Column(name = "last_period_start_date", nullable = false)
    private LocalDate lastPeriodStartDate;

    @Column(name = "onboarding_completed_at")
    private Instant onboardingCompletedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public int getTypicalCycleLengthDays() { return typicalCycleLengthDays; }
    public void setTypicalCycleLengthDays(int typicalCycleLengthDays) { this.typicalCycleLengthDays = typicalCycleLengthDays; }
    public int getTypicalPeriodDurationDays() { return typicalPeriodDurationDays; }
    public void setTypicalPeriodDurationDays(int typicalPeriodDurationDays) { this.typicalPeriodDurationDays = typicalPeriodDurationDays; }
    public LocalDate getLastPeriodStartDate() { return lastPeriodStartDate; }
    public void setLastPeriodStartDate(LocalDate lastPeriodStartDate) { this.lastPeriodStartDate = lastPeriodStartDate; }
    public Instant getOnboardingCompletedAt() { return onboardingCompletedAt; }
    public void setOnboardingCompletedAt(Instant onboardingCompletedAt) { this.onboardingCompletedAt = onboardingCompletedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public boolean isOnboardingCompleted() { return onboardingCompletedAt != null; }
}
