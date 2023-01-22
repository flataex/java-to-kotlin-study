package chapter6.kotlin.v1

import chapter6.java.Journey
import chapter6.java.Location
import chapter6.java.Other
import chapter6.java.Route
import java.util.*

object Suffering {
    @JvmStatic
    fun sufferScoreFor(route: List<Journey>): Int {
        return sufferScore(
            route.longestJourneys(3),
            Route.getDepartsFrom(route)
        )
    }

    @JvmStatic
    fun List<Journey>.longestJourneys(
        limit: Int
    ): List<Journey> = sortedByDescending { it.duration }.take(limit)

    fun routesToShowFor(itineraryId: String?): List<List<Journey>> {
        return bearable(Other.routesFor(itineraryId))
    }

    private fun bearable(routes: List<List<Journey>>): List<List<Journey>> =
        routes.filter { sufferScoreFor(it) <= 10 }

    private fun sufferScore(
        longestJourneys: List<Journey>,
        start: Location
    ): Int {
        return Other.SOME_COMPLICATED_RESULT()
    }

    inline fun <reified E> sorted(
        collection: Collection<E>,
        by: Comparator<in E>?
    ): List<E> {
        val result = collection.toTypedArray()
        Arrays.sort(result, by)
        return Arrays.asList(*result)
    }
}