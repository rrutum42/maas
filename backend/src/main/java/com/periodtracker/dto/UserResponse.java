package com.periodtracker.dto;

public record UserResponse(Long id, String displayName, boolean onboardingCompleted) {
}
