package travelator

import java.time.Duration

object KotlinLegs {
    fun List<KotlinLeg>.longestLegOver(
        legs: List<KotlinLeg>,
        duration: Duration
    ): KotlinLeg? {
        val longestLeg = maxByOrNull(KotlinLeg::plannedDuration)
        return when {
            longestLeg == null -> null
            longestLeg.plannedDuration() > duration -> longestLeg
            else -> null
        }
    }


    private fun isLongerThan(leg: KotlinLeg, duration: Duration): Boolean {
        return leg.plannedDuration() > duration
    }
}