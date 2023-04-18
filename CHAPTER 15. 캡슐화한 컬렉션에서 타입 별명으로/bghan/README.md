# 캡슐화한 컬렉션에서 타입 별명으로

## 15.1 도메인 컬렉션 합성하기

> [typealias](https://thdev.tech/kotlin/2020/10/13/kotlin_effective_06/) : 코틀린 자료 구조에 별명 정의<br>
> ex) typealias Route = List<Journey>

## 15.2 다른 프로퍼티가 있는 컬렉션

```kotlin
class Itinerary(
    val id: Id<Itinerary>,
    val route: Route
) {
    ...
}
```

- `Itinerary`를 entity 취급할 수 있는 Id 타입의 프로퍼티 존재<br>
&rArr; 자신 내부에 있는 컬렉션으로 치환 x<br>
&rArr; List<Itinerary> 를 구현하면 캡슐화되지 않은 컬렉션 장점

## 15.3 캡슐화된 컬렉션 리팩토링하기

```java
public class Route {
    private final List<Journey> journeys;
    
    public Route(List<Journey> journeys) {
        this.journeys = journeys;
    }
    
    public int size() {
        return this.journeys.size();
    }
    
    public Journey get(int index) {
        return this.journeys.get(index);
    }
    
    public Location getDepartsFrom() {
        return this.get(0).getDeparsFrom();
    }
}
```

### 15.3.1 연산을 확장으로 변환하기

<table>
<tr>
<td align="center">AS-IS</td><td align="center">TO-BE</td>
</tr>
<tr>
<td>

```kotlin
import javax.xml.stream.Location

class Route(
    private val journeys: List<Journey>
) {
    fun size(): Int = journeys.size

    operator fun get(index: Int) = journeys[index]

    val departsFrom: Location
        get() = get(0).departsFrom
}
```
</td>
<td>

```kotlin
class Route(
    val journeys: List<Journey>
)

val Route.size: Int
    get() = journeys.size

operator fun Route.get(index: Int) = journeys[index]
```
</td>
</tr>
</table>

### 15.3.2 타입 별명 치환

> 한 가지 데이터 구조에 사용할 수 있는 함수 100개가 열 가지 데이터 구조에 사용할 수 있는 함수 10개보다 더 낫다.

<table>
<tr>
<td align="center">AS-IS</td><td align="center">TO-BE</td>
</tr>
<tr>
<td>

```kotlin
fun Route.withJourneyAt(index: Int, replacedBy: Jounrey): Route {
    val newJourneys = ArrayList(journeys)
    newJourneys[index] = replacedBy
    
    return Route(newJourneys)
}
```
</td>
<td>

```kotlin
fun Route.withJourneyAt(index: Int, replacedBy: Journey): Route =
    Route(journeys.withItemAt(index, replacedBy))
```

```kotlin
fun <T> Iterable<T>.withItemAt(index: Int, replacedBy: T): List<T> =
    this.toMutableList().apply {
        this[index] = replacedBy
    }
```
</td>
</tr>
</table>

- Route가 journeys를 감싸기 때문에 journey에 직접 연산 불가능<br>
&rArr; `Route = List<Journey>` 라는 타입 별명을 통해 문제 해결 가능

<table>
<tr>
<td align="center">AS-IS</td><td align="center">TO-BE</td>
</tr>
<tr>
<td>

```kotlin
val route = Route(listOf(journey1, journey2, journey3))
val replacement = Journey(alton, alresford, someTime(), someTime(), RAIL)

assertEquals(
    listof(journey1, journey2, journey3),
    route.withJourneysAt(1, replacement).journeys
)
```
</td>
<td>

```kotlin
val route = listof(journey1, journey2, journey3)
val replacement = Journey(alton, alresford, someTime(), someTime(), RAIL)

assertEquals(
    listof(joureny1, replacement, journey3),
    route.withJourneyAt(1, replacement)
)
```
</td>
</tr>
</table>

### 15.3.3 다른 프로퍼티와 함께 있는 컬렉션 리팩터링하기

<table>
<tr>
<td align="center">AS-IS</td><td align="center">TO-BE</td>
</tr>
<tr>
<td>

```kotlin
class Itinerary(
    val id: Id<Itineray>,
    val route: Route
) {
    fun hasJourneyLongerThan(duration: Duration) = 
        route.any { it.duration > duration }
}
```
</td>
<td>

```kotlin
class Itinerary(
    val id: Id<Itinerary>,
    val route: Route
) : Route by route {
    fun hasJourneyLongerThan(duration: Duration) =
        any { it.duration > duration }
}
```
</td>
</tr>
</table>

```kotlin
fun Route.withoutJourneysBy(travelMethod: travelMethod) =
    this.filterNot { it.method == travelMethod }
```

```kotlin
fun Itinerary.withoutJourneysBy(travelMethod: TravelMethod) =
    Itinerary(
        id,
        this.filterNot { it.method == travelMethod }
    )

// data class 선언 후 아래와 같이 구현 가능
fun Itinerary.withoutJourneysBy(travelMethod: TravelMethod) =
    copy(route - filterNot{ it.method == travelMethod })
```
