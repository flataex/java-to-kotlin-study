package travelator;

import java.time.Duration;
import java.time.ZonedDateTime;

public class Leg {
    private String description;
    private ZonedDateTime plannedStart;
    private ZonedDateTime plannedEnd;

    public Leg(String description, ZonedDateTime start, ZonedDateTime plus) {
    }

    public Duration getPlannedDuration() {
        return Duration.between(plannedStart, plannedEnd);
    }

    public String getDescription() {
        return description;
    }
}
