import java.time.Duration

fun longestLegOver(
    legs: List<Leg>,
    duration: Duration
): Leg? {
    var longestLeg: Leg? = legs.maxByOrNull(Leg::plannedDuration)

    return if (longestLeg != null && longestLeg.plannedDuration > duration)
        longestLeg
    else
        null
}

private fun Leg.isLongerThan(duration: Duration) =
    plannedDuration.compareTo(duration) > 0