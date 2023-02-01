package chapter7.java.v1;

import java.util.Optional;
import java.util.stream.Collectors;

public class Tracking implements ITrackTrips {

    private final Trips trips;

    public Tracking(Trips trips) {
        this.trips = trips;
    }

    @Override
    public Optional<Trip> currentTripFor(String customerId) {
        var candidates = trips.currentTripsFor(customerId).stream()
                .filter(trip -> trip.getBookingStatus() == Trip.BookingStatus.BOOKED)
                .collect(Collectors.toList());
        if (candidates.size() == 1) {
            return Optional.of(candidates.get(0));
        } else if (candidates.size() == 0) {
            return Optional.empty();
        } else {
            throw new IllegalStateException("Unexpectedly more than one current trip for " + customerId);
        }
    }
}
