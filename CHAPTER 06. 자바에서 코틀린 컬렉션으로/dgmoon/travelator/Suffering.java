package travelator.ch6;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;
import static travelator.ch6.Routes.*;

import java.util.List;

public class Suffering {
    public static int sufferScoreFor(List<Journey> route) {
        return sufferScore(
            longestJourneysIn(route, 3),
            getDepartsFrom(route));
    }

    public static List<Journey> longestJourneysIn(
        List<Journey> journeys,
        int limit
    ) {
        var actualLimit = Math.min(journeys.size(), limit);
        return sorted(journeys,
            comparing(Journey::getDuration).reversed()
        ).subList(0, actualLimit);
    }

    public static List<List<Journey>> routesToShowFor(String itineraryId) {
        var routes = routesFor(itineraryId);
        bearable(routes);
        return routes;
    }

    private static List<List<Journey>> bearable(
        List<List<Journey>> routes
    ) {
        return routes.stream()
            .filter(route -> sufferScoreFor(route) <= 10)
            .collect(toUnmodifiableList());
    }

    private static int sufferScore(
        List<Journey> longestJourneys,
        Location start
    ) {
        return SOME_COMPLICATED_RESULT();
    }
}
