package chapter8.kotlin.v3

import chapter8.kotlin.v1.HasPrice
import chapter8.kotlin.v1.HasRating
import chapter8.kotlin.v1.HasRelevance

fun <T> Iterable<T>.sorted(ordering: Comparator<in T>): List<T> = sortedWith(ordering)

fun <T> Iterable<T>.withoutItemAt(index: Int): List<T> = take(index) + drop(index + 1)


fun byRating(): Comparator<HasRating> = Comparator.comparingDouble(HasRating::rating).reversed()

fun byPriceLowToHigh(): Comparator<HasPrice> = Comparator.comparing(HasPrice::price)

fun <T> byValue(): Comparator<T> where T : HasPrice?, T : HasRating? =
    Comparator.comparingDouble { t: T -> t!!.rating / t.price }.reversed()

fun byRelevance(): Comparator<HasRelevance> = Comparator.comparingDouble(HasRelevance::relevance).reversed()
