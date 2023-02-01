package chapter8.java.v1;

import chapter8.kotlin.v1.HasPrice;
import chapter8.kotlin.v1.HasRating;
import chapter8.kotlin.v1.HasRelevance;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class Shortlists {

    public static <T> List<T> sorted(
            List<T> shortlist,
            Comparator<? super T> ordering
    ) {
        return shortlist.stream()
                .sorted(ordering)
                .toList();
    }

    public static <T> List<T> removeItemAt(List<T> shortlist, int index) {
        return Stream.concat(
                        shortlist.stream().limit(index),
                        shortlist.stream().skip(index + 1)
                )
                .toList();
    }

    public static Comparator<HasRating> byRating() {
        return Comparator.comparingDouble(HasRating::getRating).reversed();
    }

    public static Comparator<HasPrice> byPriceLowToHigh() {
        return Comparator.comparing(HasPrice::getPrice);
    }

    public static <T extends HasPrice & HasRating> Comparator<T> byValue() {
        return Comparator.comparingDouble((T t) -> t.getRating() / t.getPrice()).reversed();
    }

    public static Comparator<HasRelevance> byRelevance() {
        return Comparator.comparingDouble(HasRelevance::getRelevance).reversed();
    }
}
