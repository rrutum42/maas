package com.periodtracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
        name = "symptom_entries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "log_date", "symptom_id"})
)
public class SymptomEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "log_date", nullable = false)
    private String logDate;

    @Column(name = "symptom_id", nullable = false)
    private Integer symptomId;

    @Column(nullable = false)
    private Integer severity;

    @Column(name = "period_log_id")
    private Long periodLogId;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getLogDate() { return logDate; }
    public void setLogDate(String logDate) { this.logDate = logDate; }
    public Integer getSymptomId() { return symptomId; }
    public void setSymptomId(Integer symptomId) { this.symptomId = symptomId; }
    public Integer getSeverity() { return severity; }
    public void setSeverity(Integer severity) { this.severity = severity; }
    public Long getPeriodLogId() { return periodLogId; }
    public void setPeriodLogId(Long periodLogId) { this.periodLogId = periodLogId; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
