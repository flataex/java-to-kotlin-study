package chapter7.kotlin.v1

import chapter7.java.v2.Trip
import chapter7.java.v2.Trips
import java.time.Clock
import java.time.Instant
import java.util.stream.Collectors

class InMemoryTrips(private val clock: Clock) : Trips {
    private val trips: MutableMap<String, MutableSet<Trip>> = HashMap()
    fun addTrip(trip: Trip) {
        val existingTrips = trips.getOrDefault(
            trip.customerId,
            HashSet()
        )
        existingTrips.add(trip)
        trips[trip.customerId] = existingTrips
    }

    override fun tripsFor(customerId: String): Set<Trip> {
        return trips.getOrDefault(customerId, emptySet())
    }

    override fun currentTripsFor(customerId: String, at: Instant): Set<Trip> {
        return tripsFor(customerId).stream()
            .filter { trip: Trip -> trip.isPlannedToBeActiveAt(at) }
            .collect(Collectors.toSet())
    }
}