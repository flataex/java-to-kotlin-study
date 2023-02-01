package main;

import java.time.Duration;

public class Leg {
    private String description;
    private Duration plannedDuration;

    public Duration getPlannedDuration() {
        return plannedDuration;
    }

    public String getDescription() {
        return this.description;
    }

    public Leg(String description, Duration plannedDuration) {
        this.description = description;
        this.plannedDuration = plannedDuration;
    }
}
