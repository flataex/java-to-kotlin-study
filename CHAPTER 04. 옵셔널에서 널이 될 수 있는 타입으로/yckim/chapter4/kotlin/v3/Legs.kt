package chapter4.kotlin.v3

import chapter4.kotlin.Leg
import java.time.Duration

fun List<Leg>.longestOver(
    legs: List<Leg>,
    duration: Duration
): Leg? =
    legs.maxByOrNull(Leg::plannedDuration)?.takeIf { longestLeg ->
        longestLeg.plannedDuration > duration
    }
