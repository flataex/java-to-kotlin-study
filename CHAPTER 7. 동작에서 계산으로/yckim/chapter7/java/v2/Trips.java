package chapter7.java.v2;

import java.time.Instant;
import java.util.Set;

public interface Trips {

    Set<Trip> tripsFor(String customerId);

    Set<Trip> currentTripsFor(String customerId, Instant at);
}
