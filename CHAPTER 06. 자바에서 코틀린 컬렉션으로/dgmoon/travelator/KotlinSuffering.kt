package travelator.ch6

import java.util.stream.Collectors

object KotlinSuffering {
    @JvmStatic
    fun sufferScoreFor(route: List<Journey?>): Int {
        return sufferScore(
            route.longestJourneys(limit = 3),
            Routes.getDepartsFrom(route)
        )
    }

    @JvmStatic
    fun longestJourneysIn(
        journeys: List<Journey>,
        limit: Int
    ): List<Journey> =
        journeys.sortedByDescending { it.duration }.take(limit)

    fun routesToShowFor(itineraryId: String?): List<List<Journey>> {
        return bearable(Other.routesFor(itineraryId))
    }

    private fun bearable(
        routes: List<List<Journey?>>
    ): List<List<Journey?>> {
        return routes.stream()
            .filter { route: List<Journey?> -> sufferScoreFor(route) <= 10 }
            .collect(Collectors.toUnmodifiableList())
    }

    private fun sufferScore(
        longestJourneys: List<Journey>,
        start: Location
    ): Int {
        return SOME_COMPLICATED_RESULT()
    }
}