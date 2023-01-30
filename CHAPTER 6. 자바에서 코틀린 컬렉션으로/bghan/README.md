# 자바에서 코틀린 컬렉션으로

## 6,1 자바 컬렉션

```java
public static int sufferScoreFor(List<Journey> route) {
    List<Journey> longestJourneys = longestJourneysIn(route, 3);
    
    return sufferScore(longestJourneys, getDepartsFrom(route)); 
}
```

```java
public static Location getDepartsFrom(List<Journey> route) {
    return route.get(0).getDepartsFrom(); // 오류 발생
}
```

```java
public static List<Journey> longestJourneysIn(List<Journey> journeys, int limit) {
    journeys.sort(comparing(Journey::getDuration).reversed()); // journeys data 변경
    
    var actualLimit = Math.min(journeys.size(), limit);
    
    return journeys.subList(0, actualLimit);
}
```

- `Collections.unmodifiableList` 를 통해 불변 리스트를 만드는게 가능하지만 List로의 unWrapping 시에 같은 이슈 발생 가능<br>
&rarr; **데이터를 생성하되 변경하지는 않도록 권장**

## 6.2 코틀린 컬렉션

> Kotlin Collection에서 Java와 같은 Collection 연산을 제공하고 상태 변경 메서드도 제공 ex) MutableCollection, MutableList

```kotlin
val aList: List<String> = SomeJavaCodes.mutableListOfStrings("0", "1")
aList.removeAt(1) // compile error
```

- downcast 가능

```kotlin
val aList: MutableList<String> = SomeJavaCodes.mutableListOfStrings("0", "1")
aList.removeAt(1)

assertEquals(listOf("0"), aList)
```

```kotlin
val aList: List<String> = SomeJavaCode.mutableListOfStrings("0", "1")
val aMutableList: MutableList<String> = aList as MutableList<String>

aMutableList.removeAt(1)
assertEquals(listOf("0"), aMutableList)
```

- `kotlin.collections.List` 임에도 immutable 보장 x

예시 1)

```kotlin
val aMutableList: MutableList<String> = mutableListOf("0", "1")
SomeJavaCode.needsAList(aMutableList)  // 함수 인자로 형변환 가능

val aList: List<String> = listOf("0", "1")
SomeJavaCode.needsAlist(aList)
```

예시 2)

```kotlin
val aMutableList = mutableListOf("0", "1")
val aList:List<String> = aMutableList

class AValueType(
    val strings: List<String>
) {
    val first: String? = strings.firstOrNull()
}

val holdsState = AValueType(aList)

aMutableList[0] = "banana"

assertEquals(holdsState.first, holdsState.strings.first()) // "0" != "banana" 로 실패 <- strings에 MutableList 참조가 남아있기 때문
```

- 가변 Collection이 불변인 Collection을 상속하게 했기 때문에 발생한 문제<br>
&rarr; 불변 Collection과 가변 Collection 사이에 하위 타입 관계를 없애고 분리 필요 ex) StringBuilder != String 하위 타입<br>
&rarr; 공유된 Collection 변경 x<br>
&rarr; 방금 생성한 Collection만 변경

```kotlin
inline fun <T, R> Iterable<T>.map(transform: (T) -> R): List<R> {
    val result = ArrayList<R>()
    
    for (item in this)
        result.add(transform(item))
    
    return result
}
```

- List에 element를 삽입한 후 read-only list로 반환<br>
&rarr; 물론 `MutableList`로 변환할 수도 있지만 그렇게 하면 안됨

## 6.3 자바에서 코틀린 컬렉션으로 리팩터링하기

> 공유된 Collection을 변경하지 말라

### 6.3.1 자바 코드 고치기

> 가변 데이터 &rarr; 계산하여 만든 새로운 데이터를 참조(= 원본 데이터는 유지)

<table>
<tr>
<td>AS-IS</td><td>TO-BE</td>
</tr>
<tr>
<td>

```java
public class Suffering {
    public static int sufferScoreFor(List<Journey> route) {
        List<Journey> longestJourneys = longestJourneysIn(route, 3);
    
        return sufferScore(longestJourneys, getDepartsFrom(route)); 
    }

    public static Location getDepartsFrom(List<Journey> route) {
        return route.get(0).getDepartsFrom();
    }

    public static List<Journey> longestJourneysIn(List<Journey> journeys, int limit) {
        journeys.sort(comparing(Journey::getDuration).reversed()); // journeys data 변경
    
        var actualLimit = Math.min(journeys.size(), limit);
    
        return journeys.subList(0, actualLimit);
    }
}
```

</td>

<td>

```java
public static <E> List<E> sorted(
        Collection<E> collection,
        Comparator<? super E> by
) {
    var result = (E[]) collection.toArray();
    
    Arrays.sort(result, by);
    
    return Arrays.asList(result);
}

public class Suffering {
    public static List<Journey> longestJourneysIn(List<Journey> journeys, int limit) {
        var actualLimit = Math.min(journeys.size(), limit);

        return sorted(
                journes,
                comparing(Journey::getDuration).reversed()
        ).subList(0, actualLimit);
    }

    public static int sufferScoreFor(List<Journey> route) {
        return sufferScore(
                longestJourneysIn(route, 3),
                getDepartsFrom(route)
        );
    }
}
```

</td>
</tr>

<tr>
<td>

```java
public static List<List<Journey>> routesToShowFor(String itineraryId) {
    var routes = routesFor(itineraryId);
    
    removeUnbearableRoutes(routes);
    
    return routes;
}

public static void removeUnbearableRoutes(List<List<Journey>> routes) {
    routes.removeIf(route -> sufferScoreFor(route) > 10);
}
```
</td>
<td>

```java
public static List<List<Journey>> routesToShowFor(String itineraryId) {
    var routes = routesFor(itineraryId);

    routes = bearable(routes);

    return routes;
}

public static List<List<Journey>> bearable(List<List<Journey>> routes) {
    return routes.stream()
        .filter(route -> sufferScoreFor(route) <= 10)
        .collect(toUnmodifiableList());
}
```

</td>
</tr>
</table>

### 6.3.2 코틀린으로 변환하기

#### 1. Java &rarr; Kotlin 변환

```kotlin
object Suffering {
    @JvmStatic
    fun sufferScoreFor(route: List<Journey>): Int {
        return sufferScore(
            longestJourneysIn(route, 3),
            Routes.getDepartsFrom(route)
        )
    }
    
    @JvmStatic
    fun longestJourneysIn(
        journeys: List<Journey>,
        limit: Int
    ): List<Journey> {
        val actualLimit = Math.min(journeys.size, limit)
        
        return sorted(
            journeys,
            comparing { obj: Journey -> obj.duration }.reversed()
        ).subList(0, actualLimit)
    }
    
    fun routesToShowFor(itineraryId: String?): List<List<Journey>> {
        return bearable(Other.routesFor(itineraryId))
    }
    
    private fun bearable(routes: List<List<Journey>>): List<List<Journey>> {
        return routes.stream()
            .filter { route -> sufferScoreFor(route) <= 10 }
            .collect(Collectors.toUnmodifiableList())
    }
    
    private fun sufferScore(
        longestJourneys: List<Journey>,
        start: Location
    ): Int {
        return SOME_COMPLICATED_RESULT()
    }
}
```

#### 2. Kotlin Utility Method로 단순화

```kotlin
@JvmStatic
fun longestJourneysIn(journeys: List<Journey>, limit: Int): List<Journey> =
    journeys.sortedDescending{ it.duration }.take(limit)

@JvmStatic
fun List<Journey>.longestJourneys(limit: Int): List<Journey> =
    sortedByDescending { it.duration }.take(limit)
```

```kotlin
private fun bearable(routes: List<List<Journey>>): List<List<Journey>> = 
    routes.filter { sufferScoreFor(it) <= 10 }
```

```kotlin
private fun sufferScore(
    longestJourneys: List<Journey>,
    start: Location
): Int = SOME_COMPLICATED_RESULT()
```

- Java에선`Collectors.toUnmodifiableList()` 필요하지만, kotlin `filter` 는 **읽기 전용 List(= view)** 로 반환

