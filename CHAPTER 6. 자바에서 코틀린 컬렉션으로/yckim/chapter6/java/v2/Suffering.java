package chapter6.java.v2;

import chapter6.java.Journey;
import chapter6.java.Location;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static chapter6.java.Other.SOME_COMPLICATED_RESULT;
import static chapter6.java.Other.routesFor;
import static chapter6.java.Route.getDepartsFrom;

public class Suffering {
    public static int sufferScoreFor(List<Journey> route) {
        return sufferScore(
                longestJourneysIn(route, 3),
                getDepartsFrom(route)
        );
    }

    public static List<Journey> longestJourneysIn(
            List<Journey> journeys,
            int limit
    ) {
        var actualLimit = Math.min(journeys.size(), limit);

        return sorted(
                journeys,
                Comparator.comparing(Journey::getDuration).reversed()
        ).subList(0, actualLimit);
    }

    public static List<List<Journey>> routesToShowFor(String itineraryId) {
        return bearable(routesFor(itineraryId));
    }

    private static List<List<Journey>> bearable(List<List<Journey>> routes) {
        return routes.stream()
                .filter(route -> sufferScoreFor(route) <= 10)
                .toList();
    }

    private static int sufferScore(
            List<Journey> longestJourneys,
            Location start
    ) {
        return SOME_COMPLICATED_RESULT();
    }

    @SuppressWarnings("unchecked")
    public static <E> List<E> sorted(
            Collection<E> collection,
            Comparator<? super E> by
    ) {
        var result = (E[]) collection.toArray();
        Arrays.sort(result, by);
        return Arrays.asList(result);
    }
}
