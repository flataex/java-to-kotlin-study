# 스트림에서 이터러블이나 시퀀스로

## 13.1 자바 스트림

#### 예제) 공백이 포함된 문자열의 평균 길이 계산

<table>

<tr>
<td align="center">v1</td><td align="center">v2</td><td align="center">v3</td>
</tr>
<tr>
<td>

```java
public static double averageNonBlankLength(List<String> strings) {
    var sum = 0;
    
    for (var s : strings) {
        if (!s.isBlank())
            sum += s.length();
    }
    
    return sum / (double) strings.size();
}
```
</td>
<td>

```java
public static double averageNonBlankLength(List<String> strings) {
    return strings
        .stream()
        .filter(s -> !s.isBlank())
        .mapToInt(String::length)
        .sum()
        / (double) strings.size();
}
```
</td>
<td>

```java
public static double averageNonBlankLength(List<String> strings) {
    return strings
        .parallelStream()
        .filter(s -> !s.isBlank())
        .mapToInt(String::length)
        .sum() 
        / (double) strings.size();
}
```
</td>
</tr>
</table>

## 13.2 코틀린 이터러블

> [Kotlin Iterable vs Sequence](https://origogi.github.io/kotlin/Iterator-vs-Sequence/)<br>
> `Iterable`<br>
> - 중간 연산 시 새로운 객체 생성<br>
> 
> `Sequence`<br>
> - 중간 연산 시 새로운 객체 생성 x<br>
> - 종단 함수(= `sum`, `toList()` 처럼 다른 값을 리턴하는 함수)가 선언돼야 Sequence 수행 &rarr; lazy evaluation

```kotlin
fun averageNonBlankLength(strings: List<String>): Double =
    (strings
        .filter { it.isNotBlank() }
        .map(String::length)
        .sum()
            / strings.size.toDouble())
```

- kotlin `filter/map` return List != java `filter/map` return Iterable
- `.filter { it.isNotBlank() }` 연산을 통해 비어있지 않은 문자열로 이뤄진 List, `.map(String::length)` 연산을 통해 문자열 길이로 구성된 List 생성 &rarr; 리스트 생성 &uarr;  &rarr; 리스트 생성을 위한 시간과 유지하기 위한 메모리 &uarr; 
- kotlin에선 **collection 크기가 크지 않다면 빠르게 작동**

[Sequence vs Stream에 대하여](https://typealias.com/guides/when-to-use-sequences/)

> 성능에 신경을 써야 할까? 대부분은 그렇지 않다. 이 코틀린 코드는 컬렉션 크기가 크지 않다면 빠르게 작동한다.<br>
> 이런 성격은 컬렉션이 긴 경우에만 빨라지는 자바 스트림과는 반대다.<br>
> 컬렉션 이 크다면 코틀린에서는 시퀀스로 전환할 수 있다.

## 13.3 코틀린 시퀀스

> 모든 연산(filter, map, sum) Sequence의 확장 (&ne; Iterable) &rarr; return Sequence(&ne; List)

<table>
<tr>
<td align="center">v1</td><td align="center">v2</td>
</tr>
<tr>
<td>

```kotlin
fun averageNonBlankLength(strings: List<String>): Double =
    (strings
        .asSequence()
        .filter { it.isNotBlank() }
        .map(String::length)
        .sum()
            / strings.size.toDouble())
```
</td>
<td>

```kotlin
fun averageNonBlankLength(strings: List<String>): Double =
    (strings
        .asSequence()
        .filter { it.isNotBlank() }
        .sumBy(String::length) // same with .map(String::length).sum() 
            / strings.size.toDouble())
```
</td>
</tr>
</table>

## 13.4 다중 이터레이션

<table>
<tr>
<td align="center">error</td><td align="center">fix</td>
</tr>
<tr>
<td>

```kotlin
// java.lang.IllegalStateException: This sequence can be consumed only once 발생
fun averageNonBlankLength(strings: List<String>): Double =
    averageNonBlankLength(strings.asSequence())

fun averageNonBlankLength(strings: Sequence<String>): Double =
    (strings
        .filter { it.isNotBlank() }
        .sumBy(String::length)
            / strings.count().toDouble())
```
</td>
<td>

```kotlin
fun averageNonBlankLength(strings: Sequence<String>): Double {
    var count = 0
    
    return (strings
        .onEach { count++ }
        .filter { it.isNotBlank() }
        .sumBy(String::length) 
        / count.toDouble())
}
```
</td>
</tr>
<tr>
<td colspan="2">

```kotlin
class CountingSequence<T>(
    private val wrapped: Sequence<T>
) : Sequence<T> {
    var count = 0
    
    override fun iterator() =
        wrapped.onEach { count++ }.iterator()
}

fun averageNonBlankLength(strings: Sequence<String>): Double {
    val countingSequence = CountingSequence(strings)
    
    return (countingSequence
        .filter { it.isNotBlank() }
        .sumBy(String::length)
            / countingSequence.count.toDouble())
}
```
</td>
</tr>
</table>

#### 왜 `java.lang.IllegalStateException: This sequence can be consumed only once` 오류가 발생할까?

- `strings.count()`가 0을 반환해 0으로 나누기 에러 발생

## 13.5 스트림, 이터러블, 시퀀스 사이에 선택하기

#### 지연 계산이 필요한 경우

- 입력을 읽는 작업을 다 끝내기 전에 결과를 얻어야할 경우
- 메모리 용량보다 더 큰 데이터를 처리해야할 경우
- 파이프라인 단계가 긴 큰 컬렉션
- 파이프라인 뒤쪽 단계에서만 얻을 수 있는 정보를 활용해 파이프라인 앞쪽 단계에서 일부를 건너뛸 수 있는 경우

<table>
<tr>
<td align="center">java</td><td align="center">kotlin</td>
</tr>
<tr>
<td>

```java
public static List<String> translateWordsUntilSTOP(List<String> strings) {
    return strings
        .stream()
        .map(word -> translate(word))
        .takeWhile(translation -> !translation.equalsIgnoreCase("STOP"))
        .collect(toList());
}
```
</td>
<td>

```kotlin
fun translateWordsUntilSTOP(strings: List<String>): List<String> =
    strings
        .map { translate(it) }
        .takeWhile { !it.equals("STOP", ignoreCase = true) }  // 모든 단어를 map을 사용해 다른 리스트로 변환 필요
```
</td>
</tr>
<tr>
<td colspan="2">

```kotlin
fun translateWordsUntilSTOP(strings: List<String>): List<String> =
    strings
        .asSequence()                                        // Sequence를 사용하여 변환할 필요가 없는 문자열 변환 x
        .map { traslate(it) }
        .takeWhile { !it.equals("STOP", ignoreCase = true) }
        .toList()
```
</td>
</tr>
</table>

## 13.6 대수적 변환

<table>
<tr>
<td align="center">v1</td><td align="center">v2</td><td align="center">v3</td>
</tr>
<tr>
<td>

```kotlin
fun averageNonBlankLength(strings: Sequence<String>): Double {
    val countingSequence = CountingSequence(strings)
    
    return (countingSequence
        .filter { it.isNotBlank() }
        .sumBy(String::length)
            / countingSequence.count.toDouble())
}
```
</td>
<td>

```kotlin
fun averageNonBlankLength(strings: Sequence<String>): Double =
    strings
        .map { if (it.isBlank()) 0 else it.length }
        .average()
```
</td>
<td>

```kotlin
fun averageNonBlankLength(strings: Sequence<String>): Double =
    strings.averageBy {
        if (it.isBlank()) 0 else it.length
    }
```
</td>
</tr>
</table>

#### averageBy란

> parameter를 바탕으로 평균값 반환

```kotlin
inline fun <T> Sequence<T>.averageBy(selector: (T) -> Int): Double {
    var sum: Double = 0.0
    var count: Int = 0
    
    for (element in this) {
        sum += selector(element)
        checkCountOverflow(++count)
    }
    
    return if (count == 0) Double.Nan else sum / count
}
```

<table>
<tr>
<td align="center">v1</td><td align="center">v2</td>
</tr>
<tr>
<td>

```java
public static double averageNonBlankLength(List<String> strings) {
    var sum = 0;
    
    for (var s : strings) {
        if (!s.isBlank())
            sum += s.length();
    }
    
    return sum / (double) strings.size();
}
```
</td>
<td>

```java
public static double averageNonBlankLength(List<String> strings) {
    return strings
        .stream()
        .filter(s -> !s.isBlank())
        .mapToInt(String::length)
        .sum()
        / (double) strings.size();
}
```
</td>
</tr>
<tr>

<td>

```java
public static double averageNonBlankLength(List<String> strings) {
    var sum = 0;
    
    for (var s : strings) {
        sum += s.isBlank() ? 0 : s.length();
    }
    
    return sum / (double) strings.size();
}
```
</td>
<td>

```java
public static double averageNonBlankLength(List<String> strings) {
    return strings
        .stream()
        .mapToInt(s -> s.isBlank() ? 0 : s.length())
        .average()
        .orElse(Double.NaN);
}
```
</td>
</tr>
</table>

## 13.7 스트림에서 이터러블이나 시퀀스로 리팩터링하기

<table>
<tr>
<td align="center">v1</td><td align="center">v2</td>
</tr>
<tr>

<td>

```kotlin
fun averageNumberOfEventsPerCompletedBooking(
    timeRange: String
): Double {
    val eventsForSuccessfulBookings = eventStore
        .queryAsStream("type=CompletedBooking&timerange=$timeRange")
        .flatMap { event: Map<String?, Any?> -> 
            val interactionId = event["interactionId"] as String?
            eventStore.queryAsStream("interactionId=$interactionId")
        }
    
    val bookingEventsByInteractionId = eventsForSuccessfulBookings.collect(
        Collectors.groupingBy(
            Function { event: Map<String, Any> -> 
                event["interactionId"] as String?
            }
        )
    )
    
    val averageNumberOfEventsPerCompletedBooking = bookingEventsByInteractionId
        .values
        .stream()
        .mapToInt{ obj: List<Map<String, Any>> -> obj.size }
        .average()
    
    return averageNumberOfEventsPerCompletedBooking.orElse(Double.NaN)
}
```
</td>
<td>

```kotlin
fun averageNumberOfEventsPerCompletedBooking(
    timeRange: String
): Double {
    val eventsForSuccessfulBookings = eventStore
        .queryAsStream("type=CompletedBooking&timerange=$timeRange")
        .flatMap{ event ->
            val interactionId = event["interactionId"] as String
            eventStore.queryAsStream("interactionId=$interactionId")
        }
    
    val bookingEventsByInteractionId = eventsForSuccessfulBookings.collect(
        groupingBy { event -> event["interactionId"] as String}
    )
    
    val averageNumberOfEventsPerCompletedBooking = bookingEventsByInteractionId
        .values
        .stream()
        .mapToInt { it.size }
        .average()
    
    return averageNumberOfEventsPerCompletedBooking.orElse(Double.NaN)
}
```
</td>
</tr>
</table>

### 13.7.1 이터러블 먼저 고려하기

<table>
<tr>
<td align="center">v1</td><td align="center">v2</td>
</tr>
<tr>
<td>

```kotlin
val values = bookingEventsByInteractionId.values
return values.sumBy { it.size } / values.size.toDouble()
```
</td>
<td>

```kotlin
val values = bookingEventsByInteractionId.values
return averageBy(values)

private fun averageBy(
    values: MutableCollection<MutableList<MutableMap<String, Any>>>
): Double {
    return values.sumBy { it.size } / values.size.toDouble()
}
```
</td>
</tr>
<tr>
<td>

```kotlin
private fun averageBy(
    values: Collection<MutableList<MutableMap<String, Any>>>,
    selector: (MutableList<MutableMap<String, Any>>) -> Int
): Double {
    return values.sumBy(selector) / values.size.toDouble()
}
```
</td>
<td>

```kotlin
private fun <T: MutableList<MutableMap<String, Any>>> averageBy(
    values: Collection<T>,
    selector: (T) -> Int
): Double {
    return values.sumBy(selector) / values.size.toDouble()
}
```
</td>
</tr>
<tr>
<td>

```kotlin
private fun <T> averageBy(
    values: Collection<T>,
    selector: (T) -> Int
): Double {
    return values.sumBy(selector) / values.size.toDouble()
}
```
</td>
<td>

```kotlin
val values = bookingEventsByInteractionId.values
return averageBy<MutableList<MutableMap<String, Any>>>(values) { it.size }

inline fun <T> Collection<T>.averageBy(selector: (T) -> Int): Double =
    sumBy(selector) / size.toDouble()
```
</td>
</tr>
</table>

### 13.7.2 그 다음 시퀀스로 변환하기

<table>
<tr>
<td align="center">v1</td><td align="center">v2</td>
</tr>
<td>

```kotlin
val bookingEventsByInteractionId = eventsForSuccessfulBookings.collect(
        groupingBy { event -> event["interactionId"] as String}
)
```
</td>
<td>

```kotlin
val bookingEventsByInteractionId = eventsForSuccessfulBookings
    .asSequence()
    .groupBy { event ->
        event["interactionId"] as String
    }
```
</td>
</tr>
<tr>
<td>

```kotlin
val eventsForSuccessfulBookings = eventStore
    .queryAsStream("type=CompletedBooking&timerange=$timeRange")
    .flatMap{ event ->
        val interactionId = event["interactionId"] as String
        eventStore.queryAsStream("interactionId=$interactionId")
    }
```
</td>
<td>

```kotlin
val eventsForSuccessfulBookings = eventStore
    .queryAsStream("type=CompletedBooking&timerange=$timeRange")
    .asSequence()
    .flatMap {event ->
        val interactionId = event["interactionId"] as String
        eventStore
            .queryAsStream("interactionId=$interactionId")
            .asSequence()
    }
```
</td>
</tr>
<tr>
<td>

```kotlin
val bookingEventsByInteractionId = eventsForSuccessfulBookings.collect(
        groupingBy { event -> event["interactionId"] as String}
)
```
</td>
<td>

```kotlin
val bookingEventsByInteractionId = eventsForSuccessfulBookings
    .asSequence()
    .groupBy { event ->
        event["interactionId"] as String
    }
```
</td>
</tr>
<tr>
<td>

```kotlin
val eventsForSuccessfulBookings = eventStore
    .queryAsStream("type=CompletedBooking&timerange=$timeRange")
    .flatMap{ event ->
        val interactionId = event["interactionId"] as String
        eventStore.queryAsStream("interactionId=$interactionId")
    }
```
</td>
<td>

```kotlin
val eventsForSuccessfulBookings = eventStore
    .queryAsStream("type=CompletedBooking&timerange=$timeRange")
    .asSequence()
    .flatMap{ event ->
        val interactionId = event["interactionId"] as String
        eventStore
            .queryAsStream("interactionId=$interactionId")
            .asSequence()
    }
```
</td>
</tr>
<tr>
<td>

```kotlin
fun averageNumberOfEventsPerCompletedBooking(
    timeRange: String
): Double {
    val eventsForSuccessfulBookings = eventStore
        .queryAsSequence("type=CompletedBooking&timerange=$timeRange")
        .flatMap { event ->
            val interactionId = event["interactionId"] as String
            eventStore
                .queryAsSequence("interactionId=$interactionId")
        }
    
    val bookingEveentsByInteractionId = eventsForSuccessfulBookings
        .groupBy { event ->
            event["interactionId"] as String
        }
    
    return bookingEveentsByInteractionId.values.averageBy { it.size }
}
```
</td>
<td>

```kotlin
fun averageNumberOfEventsPerCompletedBooking(
    timeRange: String
): Double {
    return eventStore
        .queryAsSequence("type=CompletedBooking&timerange=$timeRange")
        .flatMap { event ->
            val interactionId = event["interactionId"] as String
            eventStore
                .queryAsSequence("interactionId=$interactionId")
        }.groupBy {event ->
            event["interactionId"] as String
        }.values
        .averageBy { it.size }
}
```
</td>
</tr>
</table>

### 13.7.3 파이프라인 일부를 추출하기

<table>
<tr>
<td align="center">v1</td><td align="center">v2</td>
</tr>
<tr>
<td>

```kotlin
fun averageNumberOfEventsPerCompletedBooking(
    timeRange: String
): Double {
    return allEventsInSameInteractions(
        eventStore
            .queryAsSequence("type=CompletedBooking&timerange=$timeRange")
    )
        .groupBy { event -> 
            event["interactionId"] as String
        }.values
        .averageBy { it.size }
}

private fun allEventsInSameInteractions(
    sequence: Sequence<MutableMap<String, Any?>>
) = sequence
    .flatMap { event ->
        val interactionId = event["interactionId"] as String
        eventStore
            .queryAsSequence("interactionId=$interactionId")
    }
```
</td>
<td>

```kotlin
fun averageNumberOfEventsPerCompletedBooking(
    timeRange: Stirng
): Double {
    return eventStore
        .queryAsSequence("type=CompletedBooking&timerange=$timeRange")
        .allEventsInSameInteractinos()
        .groupBy { event ->
            event["interactionId"] as String
        }.values
        .averageBy { it.size }
}

fun Sequence<Map<String, Any?>>.allEventsInSameInteractions() =
    flatMap { event ->
        val interactionId = event["interactionId"] as String
        eventStore
            .queryAsSequence("interactionId=$interactionId")
    }
```
</td>
</tr>
</table>

### 13.7.4 마지막 정리

```kotlin
class MarketingAnalytics(
    private val eventStore: EventStore
) {
    fun averageNumberOfEventsPerCompletedBooking(
        timeRange: String
    ): Double = eventStore
        .queryAsSequence("type=CompletedBooking&timerange=$timeRange")
        .allEventsInSameInteractions()
        .groupBy(Event::interactionId)
        .values
        .averageBy { it.size }
    
    private fun Sequence<Event>.allEventsInSameInteractions() =
        flatMap { event ->
            eventStore.queryAsSeqeuence(
                "interactionId=${event.interactionId}"
            )
        }
}

inline fun <T> Collection<T>.averageBy(selector: (T) -> Int): Double = 
    sumBy(selector) / size.toDouble()

fun EventStore.queryAsSequence(query: String)=
    this.queryAsStream(query).asSequence()
```