### 목 프레임워크는 예상되는 메서드 호출과 호출시 반환하는 내용을 기술하게 해준다.

```
when(featuredDestinations.findCloseTo(paris))
    .thenReturn(List.of(
        eiffelTower,
        louvre
    ));
    
when(distancecalculator.distanceInMetersBetween(
    paris, eiffelTower.getLocation())
).thenReturn(5000);
```

<br>
<hr>

## 목을 맵으로 대체하기

```kt
class RecommendationsTests {
    private val distancecalculator = mock(DistanceCalculator::class.java)
    private val featuredDestinations = mock(FeaturedDestinations::class.java)

    private val recommendations = Recommendations(
        featuredDestinations::findCloseTo,
        distancecalculator::distancelnMetersBetween
    )
    
}
```

```kt
private fun givenFeaturedDestinationsFor(
    location: Location,
    result: List<FeaturedDestination>
) {
    Mockito.`when`(featuredDestinations.findCloseTo(location))
        .thenReturn(result)
}
```

목을 제거하는 방법은 **함수 타입을 입력 파라미터와 결과를 연관시켜주는 매핑하는 것이다.** <br>
Map에 파라미터 키와 결과 값을 넣고, 공급받은 파라미터를 맵에서 검색하는 연산으로 함수 호출을 대신한다.

```kotlin
private val featuredDestinations = 
    mutableMapOf<location, List<FeaturedDestination>>()
        .withDefault { emptyList() }


private fun givenFeaturedDestinationsFor(
    location: Location,
    result: List<FeaturedDestination>
) {
    featuredDestinations[location] = destinations.toList();
}
```

<br>

**목을 맵으로 대체한다면 테스트 코드가 단순해진다**

```kotlin
private fun check(
    featuredDestinations: Map<Location, List<Feat니redDestination>>, 
    distances: Map<Pair<Location, Location>, Int>,
    recommendations: Set<Location>,
    shouldReturn: List<FeaturedDestinationSuggestion>
) {
    assertEquals(
        shouldReturn,
        resultFor(featuredDestinations, distances, recommendations) 
    )
}

@Test
fun returns_no_recommendations_when_no_featured() {
    check(
        featuredDestinations = emptyMap(), 
        distances = distances,
        recommendations = setOf(paris),
        shouldReturn = emptyList()
    ) 
}
```

<br>
<hr>
