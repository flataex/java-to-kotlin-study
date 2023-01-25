package travelator

import java.time.Duration
import java.time.ZonedDateTime

class KotlinLeg (
    var description: String,
    private val plannedStart: ZonedDateTime,
    private val plannedEnd: ZonedDateTime
) {
    fun plannedDuration(): Duration {
        return Duration.between(plannedStart, plannedEnd)
    }

    private fun isLongerThan(leg: Leg, duration: Duration): Boolean {
        return leg.plannedDuration > duration
    }
}