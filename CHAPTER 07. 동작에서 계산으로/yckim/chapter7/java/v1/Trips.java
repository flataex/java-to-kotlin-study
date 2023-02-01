package chapter7.java.v1;

import java.util.Set;

public interface Trips {

    Set<Trip> tripsFor(String customerId);

    Set<Trip> currentTripsFor(String customerId);
}
