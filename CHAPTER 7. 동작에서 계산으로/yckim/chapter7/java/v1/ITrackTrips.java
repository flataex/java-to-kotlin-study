package chapter7.java.v1;

import java.util.Optional;

public interface ITrackTrips {
    Optional<Trip> currentTripFor(String customerId);
}
