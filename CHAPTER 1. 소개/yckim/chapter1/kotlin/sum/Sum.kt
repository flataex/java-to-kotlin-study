package chapter1.kotlin.sum
fun main() {
    val numbers = intArrayOf(1, 2, 3)

    val fold = numbers.fold(0, Int::plus)
    println(fold)

}