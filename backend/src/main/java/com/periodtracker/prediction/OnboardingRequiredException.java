package com.periodtracker.prediction;

public class OnboardingRequiredException extends RuntimeException {

    private final long userId;

    public OnboardingRequiredException(long userId) {
        super("Complete onboarding before requesting predictions");
        this.userId = userId;
    }

    public long getUserId() { return userId; }
}
