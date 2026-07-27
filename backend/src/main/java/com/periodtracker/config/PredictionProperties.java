package com.periodtracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "maas.prediction")
public class PredictionProperties {

    private int windowSize = 6;
    private int observedMinSample = 2;
    private int onboardingSigma = 4;
    private int lutealPhaseDays = 14;

    public int getWindowSize() { return windowSize; }
    public void setWindowSize(int windowSize) { this.windowSize = windowSize; }
    public int getObservedMinSample() { return observedMinSample; }
    public void setObservedMinSample(int observedMinSample) { this.observedMinSample = observedMinSample; }
    public int getOnboardingSigma() { return onboardingSigma; }
    public void setOnboardingSigma(int onboardingSigma) { this.onboardingSigma = onboardingSigma; }
    public int getLutealPhaseDays() { return lutealPhaseDays; }
    public void setLutealPhaseDays(int lutealPhaseDays) { this.lutealPhaseDays = lutealPhaseDays; }
}
