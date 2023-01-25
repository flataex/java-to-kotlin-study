package chapter4.kotlin.v2

import chapter4.kotlin.Leg
import java.time.Duration
import java.util.*

object Legs {
    fun findLongestLegOver(
        legs: List<Leg>,
        duration: Duration?
    ): Optional<Leg> {
        return Optional.ofNullable(longestLegOver(legs, duration))
    }

    fun longestLegOver(
        legs: List<Leg>,
        duration: Duration?
    ): Leg? {
        var result: Leg? = null
        for (leg in legs) {
            if (result == null || isLongerThan(leg, result.plannedDuration)) {
                result = leg
            }
        }
        return result
    }

    private fun isLongerThan(leg: Leg, duration: Duration): Boolean {
        return leg.plannedDuration.compareTo(duration) > 0
    }
}