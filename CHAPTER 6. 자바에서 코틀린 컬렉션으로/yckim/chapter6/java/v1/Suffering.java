package chapter6.java.v1;

import chapter6.java.Journey;
import chapter6.java.Location;

import java.util.List;

import static chapter6.java.Other.SOME_COMPLICATED_RESULT;
import static chapter6.java.Other.routesFor;
import static chapter6.java.Route.getDepartsFrom;
import static java.util.Comparator.comparing;

public class Suffering {
    public static int sufferScoreFor(List<Journey> route) {
        List<Journey> longestJourneys = longestJourneysIn(route, 3);
        return sufferScore(longestJourneys, getDepartsFrom(route));
    }

    public static List<Journey> longestJourneysIn(
            List<Journey> journeys,
            int limit
    ) {
        journeys.sort(comparing(Journey::getDuration).reversed()); // <1>
        var actualLimit = Math.min(journeys.size(), limit);
        return journeys.subList(0, actualLimit);
    }

    public static List<List<Journey>> routesToShowFor(String itineraryId) {
        var routes = routesFor(itineraryId);
        removeUnbearableRoutes(routes);
        return routes;
    }

    private static void removeUnbearableRoutes(List<List<Journey>> routes) {
        routes.removeIf(route -> sufferScoreFor(route) > 10);
    }

    private static int sufferScore(
            List<Journey> longestJourneys,
            Location start
    ) {
        return SOME_COMPLICATED_RESULT();
    }
}
