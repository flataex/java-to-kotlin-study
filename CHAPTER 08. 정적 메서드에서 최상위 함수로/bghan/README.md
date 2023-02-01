# 정적 메서드에서 최상위 함수로

> 최상위 함수란, 클래스에 관계 없이 가장 밖에 있는 함수

## 8.2 코틀린 최상위 함수와 동반 객체

- 코틀린은 함수를 클래스 밖에 선언 가능 &larr; 코틀린 컴파일러가 정적 멤버가 들어있는 클래스 생성해줌
- 정적 멤버와 비정적 멤버를 한 클래스 안에 모아야 하는 경우 `companion object` 내부 정의
- 동반 객체의 멤버는 동반 객체가 속한 클래스 인스턴스의 private 상태에서도 접근 가능

## 8.3 정적 메서드를 최상위 함수로 리팩터링하기

<table>

<tr>
<td>Java</td><td>Kotlin</td>
</tr>

<tr>
<td>

```java
import java.util.Comparator;

public class Shortlists {
    public static <T> List<T> sorted(
            List<T> shortList,
            Comparator<? super T> ordering
    ) {
        return shortList
                .stream()
                .sorted(ordering)
                .collect(toUnmodifiableList());
    }

    public static <T> List<T> removeItemAt(List<T> shortList, int index) {
        return Stream.concat(
                shortList.stream().limit(index),
                shortList.stream().skip(index + 1)
        ).collect(toUnmodifiableList());
    }

    public static Comparator<HasRating> byRating() {
        return comparingDouble(HasRating::getRating).reversed();
    }

    public static Comparator<HasPrice> byPriceLowToHigh() {
        return comparing(HasPrice::getPrice);
    }
}
```
</td>

<td>

```kotlin
object Shortlists {
    @JvmStatic
    fun <T> sorted(shortlist: List<T>, ordering: Comparator<in T>): List<T> {
        return shortlist
            .stream()
            .sorted(ordering)
            .collect(toUnmodifiableList())
    }
    
    @JvmStatic
    fun <T> removeItemAt(shortlist: List<T>, index: Int): List<T> {
        return Stream.concat(
            shortlist.stream().limit(index.toLong()),
            shortlist.stream().skip((index + 1).toLong())
        ).collect(toUnmodifiableList())
    }
    
    @JvmStatic
    fun byRating(): Comparator<HasRating> {
        return comparingDouble(HasRating::rating).reversed()
    }
    
    @JvmStatic
    fun byPriceLowToHigh(): Comparator<HasPrice> {
        return comparing(HasPrice::price)
    }
}
```
</td>
</tr>

</table>

- 최상위함수로 리팩토링

```kotlin
@file:JvmName("Shortlists")
package travelator

fun <T> sorted(shortlist: List<T>, ordering: Comparator<in T>): List<T> {
    return shortlist
        .stream()
        .sorted(ordering)
        .collect(toUnmodifiableList())
}

fun <T> removeItemAt(shortlist: List<T>, index: Int): List<T> {
    return Stream.concat(
        shortlist.stream().limit(index.toLong()),
        shortlist.stream().skip((index + 1).toLong())
    ).collect(toUnmodifiableList())
}
```

#### @JvmName이란,

> 코틀린을 바이트코드로 변환할 때 JVM 시그니쳐를 변경하는 용도

- `2개의 foo()` 는 바이트코드로 변경될 때 인자가 List<>이기 때문에 시그니쳐가 동일하여 컴파일 에러 발생

```kotlin
// foo.kt - Kotlin 파일명

// Compile Error 
fun foo(a : List<String>) {
    println("foo(a : List<String>")
}

fun foo(a : List<Int>) {
    println("foo(a : List<Int>")
}
```

```shell
Error:(7, 1) Kotlin: Platform declaration clash: The following declarations have the same JVM signature (foo(Ljava/util/List;)V):
    fun foo(a: List<Int>): Unit defined in foo.main.kotlin in file kotlin.kt
    fun foo(a: List<String>): Unit defined in foo.main.kotlin in file kotlin.kt
```

- `@JvmName` 을 사용해서 두 함수의 Signature를 변경 가능 &rarr; 두 함수의 이름을 서로 다르게 변경 


```kotlin
// foo.kt - kotlin 파일명

@JvmName("fooString")
fun foo(a : List<String>) {
    println("foo(a : List<String>")
}

@JvmName("fooInt")
fun foo(a : List<Int>) {
    println("foo(a : List<Int>")
}
```

## 8.5 코틀린답게 다듬기

| sortedBy              | sortedWith                    |
|:----------------------|:------------------------------|
| selector를 통해 정렬 기준 정의 | comparator을 지정해서 다양한 기준 정의 가능 |
|`planedit.sortedBy{it.first}`|`planedit.sortedWith(compareBy({ it.first }, { it.second }))`|

```kotlin
fun <T> Iterable<T>.sorted(ordering: Comparator<in T>): List<T> =
    sortedWith(ordering)

fun <T> Iterable<T>.withoutItemAt(index: Int): List<T> = 
    take(index) + drop(index + 1)

fun byRating(): Comparator<HasRating> =
    comparingDouble(HasRating::rating).reversed()

fun byPriceLowToHigh(): Comparator<HasPrice> = 
    comparing(HasPrice::price)
```

#### take

> Collection 범위를 통해 새로운 List를 만듬

```kotlin
(0..10).toList().take(3) // [0, 1, 2]
```


#### drop 

> Collection 범위를 통해 제거하여 새로운 List 만듬

```kotlin
(0..10).toList().take(3) // [3, 4, 5, 6, 7, 8, 9, 10]
```

