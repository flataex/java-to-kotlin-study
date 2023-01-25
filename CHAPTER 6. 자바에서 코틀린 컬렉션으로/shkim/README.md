## 코틀린 컬렉션

자바와 달리 코틀린과 코틀린 표준 라이브러리는 상태 변경이 유행에서 뒤떨어진 것으로 취급되는 시대에 설계됐다. <br>
하지만 자바와의 부드러운 상호 운용이 핵심 목표이고. 자바는 가변 컬렉션을 사용한다. <br>
코틀린은 어떻게 이 차이를 극복하고 불변 컬렉션이 자바 컬렉션과 매끄럽게 상호 운용되게 할 수 있을까 ?? <br>

**코틀린에서 '공유된 컬렉션을 변경하지 말라'를 설명하며 제안한 다른 방법은 코드에서 방금 생성한 컬렉션만 변화시키라는 것이다.**

<br>

## 자바에서 코틀린 컬렉션으로 리팩토링하기

> 아래의 자바 코드는 문제가 있다. <br>
> **longestJourneysIn는 파라미터를 변경한다.** <br>
> 자바의 List.sort는 실제로 자기 자신을 정렬한 복사본을 만들고, 그 복사본에 일치하도록 자신을 변경한다. <br>

```java
import java.util.List;

import static java.util.Comparator.comparing;

public class Suffering {

    public static int sufferScoreFor(
            List<Journey> route
    ) {
        List<Journey> longestJourneys = longestJourneysIn(route, 3);
        return sufferScore(longestJourneys, getDepartsFrom(route));
    }

    private static int sufferScore(List<Journey> longestJourneys, Location start) {
        return 0;
    }

    private static List<Journey> longestJourneysIn(
            List<Journey> journeys,
            int limit
    ) {
        journeys.sort(comparing(Journey::getDuration).reversed());
        int actualLimit = Math.min(journeys.size(), limit);
        return journeys.subList(0, actualLimit);
    }

    private static Location getDepartsFrom(List<Journey> route) {
        return route.get(0).getDepartsFrom();
    }
}
```

```java
import java.time.Duration;
import java.time.ZonedDateTime;

public class Journey {

    private final Location departsFrom;
    private final Location arrivesAt;
    private final ZonedDateTime departureTime;
    private final ZonedDateTime arrivalTime;


    public Journey(Location departsFrom, Location arrivesAt, ZonedDateTime departureTime, ZonedDateTime arrivalTime) {
        this.departsFrom = departsFrom;
        this.arrivesAt = arrivesAt;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    public Location getDepartsFrom() {
        return departsFrom;
    }

    public Location getArrivesAt() {
        return arrivesAt;
    }

    public ZonedDateTime getDepartureTime() {
        return departureTime;
    }

    public ZonedDateTime getArrivalTime() {
        return arrivalTime;
    }

    public Duration getDuration() {
        return Duration.between(departureTime, arrivalTime);
    }
}
```

<br>

> 먼저 sort를 리팩토링 해보자.

```java
public static <E> List<E> sorted(
    CoUection<E> collection.
    Comparators? super E> by
) {
    var result = (E[]) collection.toArray();
    Arrays.sort(result, by);
    return Arrays.asList(result);
}
```

<br>

> 그 이후 sort를 쓰는 코드를 리팩토링한다.

```java
static List<Journey> longestJourneysIn(
        List<Journey> journeys,
    int limit
) {
    var actualLimit = Math.min(journeys.size(), limit);
    return sorted(
            journeys,
            comparing(Journey::getDuration).reversed()
    ).subList(0, actualLimit);
}


public static int sufferScoreFor(List<Journey> route) { 
    return sufferScore(
        longestJourneysIn(route, 3),
        getDepartsFrom(route));
}
```

<br>

> 밖으로 나와 sufferScoreFor를 호출하는 코드는 다음과 같다. <br>
> removeUnbearableRoutes는 void를 반환하기에, 내부에서 뭔가 상태를 바꾼다는 점을 보여준다. <br>

```java
public static List<List<Journey>> routesToShowFor(String itineraryId) { 
    var routes = routesFor(itineraryId);
    removeUnbearableRoutes(routes);
    return routes;
}

private static void removeUnbearableRoutes(List<List<Journey>> routes) { 
    routes.removeIf(route -> sufferScoreFor(route) > 10);
}
```

<br>

> 이 메서드가 자신이 받은 parameter를 반환하게 변경하고, 호출하는 쪽에선 반환받은 값을 사용하도록 리팩토링한다. <br>
> 그 이후 Stream.filter를 사용해 removeUnbearableRoutes의 상태 변경을 대신하고, 메서드 이름도 바꿔준다.

```java
public static List<List<Journey>> routesToShowFor(String itineraryId) {
    return bearable(routesFor(itineraryId));
}

private static List<List<Journey>> bearable(List<List<Journey>> routes) {
        return routes.stream()
            .filter(route -> sufferScoreFor(route) <= 10)
            .collect(toUnmodifiableList());
}
```

<br>

***코틀린으로 변환하기***

> 이제 자바 컬렉션에서 상태 변경을 없앴으므로, 코틀린으로 변환하자.

```kt
import java.util.stream.Collectors

object Suffering {

    @JvmStatic
    fun sufferScoreFor(
        route: List<Journey>
    ): Int {
        return sufferScore(
            longestJourneysIn(route, 3),
            getDepartsFrom(route)
        )
    }

    @JvmStatic
    private fun longestJourneysIn(
        journeys: List<Journey>,
        limit: Int
    ): List<Journey> {
        val actualLimit = Math.min(journeys.size, limit)
        return Collections.sorted(
            journeys,
            Comparator.comparing { obj: Journey -> obj.duration }.reversed()
        ).subList(0, actualLimit)
    }

    private fun getDepartsFrom(route: List<Journey>): Location {
        return route[0].departsFrom
    }

    fun routesToShowFor(itineraryId: String): List<List<Journey>> {
        return bearable(routesFor(itineraryId))
    }

    private fun bearable(
        routes: List<List<Journey>>
    ): List<List<Journey>> {
        return routes.stream()
            .filter { route: List<Journey> -> sufferScoreFor(route) <= 10 }
            .collect(Collectors.toUnmodifiableList())
    }

    private fun routesFor(itineraryId: String): List<List<Journey>> {
        return java.util.List.of()
    }

    private fun sufferScore(
        longestJourneys: List<Journey>,
        start: Location
    ): Int {
        return SOME_COMPLICATED_RESULT()
    }

    private fun SOME_COMPLICATED_RESULT(): Int {
        return 0
    }
}
```

<br>

> 최종적으로 코틀린스럽게 리팩토링하면 다음과 같다. <br>
> 1. sorted를 sortedByDescending으로 변경하고 subList를 take로 변환한다. <br>
> 2. longestJourneysIn 함수를 확장 함수, 단일식 함수로 변환한다. <br>
> 3. bearable의 Stream을 코틀린으로 변환한다.

```kt
object Suffering {

    @JvmStatic
    fun sufferScoreFor(
        route: List<Journey>
    ): Int {
        return sufferScore(
            route.longestJourneys(limit = 3),
            getDepartsFrom(route)
        )
    }

    @JvmStatic
    fun List<Journey>.longestJourneys(limit: Int): List<Journey> =
        sortedByDescending { it.duration }.take(limit)

    private fun getDepartsFrom(route: List<Journey>): Location {
        return route[0].departsFrom
    }

    fun routesToShowFor(itineraryId: String): List<List<Journey>> {
        return bearable(routesFor(itineraryId))
    }

    private fun bearable(routes: List<List<Journey>>): List<List<Journey>> =
        routes.filter { sufferScoreFor(it) <= 10 }

    private fun routesFor(itineraryId: String): List<List<Journey>> {
        return java.util.List.of()
    }

    private fun sufferScore(
        longestJourneys: List<Journey>,
        start: Location
    ): Int {
        return SOME_COMPLICATED_RESULT()
    }

    private fun SOME_COMPLICATED_RESULT(): Int {
        return 0
    }
}
```