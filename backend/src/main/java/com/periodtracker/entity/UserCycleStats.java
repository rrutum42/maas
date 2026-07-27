package com.periodtracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "user_cycle_stats")
public class UserCycleStats {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "sample_size", nullable = false)
    private int sampleSize = 0;

    @Column(name = "avg_cycle_length")
    private Double avgCycleLength;

    @Column(name = "cycle_length_stddev")
    private Double cycleLengthStddev;

    @Column(name = "last_period_start")
    private LocalDate lastPeriodStart;

    @Column(name = "avg_period_duration")
    private Double avgPeriodDuration;

    @Column(name = "period_duration_stddev")
    private Double periodDurationStddev;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt = Instant.now();

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public int getSampleSize() { return sampleSize; }
    public void setSampleSize(int sampleSize) { this.sampleSize = sampleSize; }
    public Double getAvgCycleLength() { return avgCycleLength; }
    public void setAvgCycleLength(Double avgCycleLength) { this.avgCycleLength = avgCycleLength; }
    public Double getCycleLengthStddev() { return cycleLengthStddev; }
    public void setCycleLengthStddev(Double cycleLengthStddev) { this.cycleLengthStddev = cycleLengthStddev; }
    public LocalDate getLastPeriodStart() { return lastPeriodStart; }
    public void setLastPeriodStart(LocalDate lastPeriodStart) { this.lastPeriodStart = lastPeriodStart; }
    public Double getAvgPeriodDuration() { return avgPeriodDuration; }
    public void setAvgPeriodDuration(Double avgPeriodDuration) { this.avgPeriodDuration = avgPeriodDuration; }
    public Double getPeriodDurationStddev() { return periodDurationStddev; }
    public void setPeriodDurationStddev(Double periodDurationStddev) { this.periodDurationStddev = periodDurationStddev; }
    public Instant getComputedAt() { return computedAt; }
    public void setComputedAt(Instant computedAt) { this.computedAt = computedAt; }
}
