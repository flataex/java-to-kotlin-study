package chapter7.java.v1;

import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTrips implements Trips {

    private final Clock clock;

    private final Map<String, Set<Trip>> trips = new HashMap<>();

    public InMemoryTrips(Clock clock) {
        this.clock = clock;
    }

    public void addTrip(Trip trip) {
        var existingTrips = trips.getOrDefault(
                trip.getCustomerId(),
                new HashSet<>()
        );
        existingTrips.add(trip);
        trips.put(trip.getCustomerId(), existingTrips);
    }

    @Override
    public Set<Trip> tripsFor(String customerId) {
        return trips.getOrDefault(customerId, Collections.emptySet());
    }

    @Override
    public Set<Trip> currentTripsFor(String customerId) {
        var now = clock.instant();
        return tripsFor(customerId).stream()
                .filter(trip -> trip.isPlannedToBeActiveAt(now))
                .collect(Collectors.toSet());
    }

}
