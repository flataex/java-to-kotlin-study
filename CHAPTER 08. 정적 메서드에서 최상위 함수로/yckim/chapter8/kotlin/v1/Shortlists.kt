package chapter8.kotlin.v1

import java.util.stream.Stream

object Shortlists {
    fun <T> sorted(
        shortlist: List<T>,
        ordering: Comparator<in T>?
    ): List<T> {
        return shortlist.stream()
            .sorted(ordering)
            .toList()
    }

    fun <T> removeItemAt(shortlist: List<T>, index: Int): List<T> {
        return Stream.concat(
            shortlist.stream().limit(index.toLong()),
            shortlist.stream().skip((index + 1).toLong())
        )
            .toList()
    }

    fun byRating(): Comparator<HasRating> {
        return Comparator.comparingDouble(HasRating::rating).reversed()
    }

    fun byPriceLowToHigh(): Comparator<HasPrice> {
        return Comparator.comparing(HasPrice::price)
    }

    fun <T> byValue(): Comparator<T> where T : HasPrice?, T : HasRating? {
        return Comparator.comparingDouble { t: T -> t!!.rating / t.price }.reversed()
    }

    fun byRelevance(): Comparator<HasRelevance> {
        return Comparator.comparingDouble(HasRelevance::relevance).reversed()
    }
}