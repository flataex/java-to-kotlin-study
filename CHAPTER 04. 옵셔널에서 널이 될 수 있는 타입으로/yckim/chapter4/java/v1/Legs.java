package chapter4.java.v1;

import chapter4.kotlin.Leg;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class Legs {

    public static Optional<Leg> findLongestLegOver(
            List<Leg> legs,
            Duration duration
    ) {
        Leg result = null;
        for (Leg leg : legs) {
            if (result == null || isLongerThan(leg, result.getPlannedDuration())) {
                result = leg;
            }
        }
        return Optional.ofNullable(result);
    }

    private static boolean isLongerThan(Leg leg, Duration duration) {
        return leg.getPlannedDuration().compareTo(duration) > 0;
    }
}
