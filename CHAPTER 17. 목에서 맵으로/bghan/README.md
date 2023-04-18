# 목에서 맵으로

> 호출되지 않은 메서드를 위한 mock 개선

## 17.1 목을 맵으로 대체하기

> Map에 parameter key와 결과값 저장

<table>
<tr>
<td align="center">AS-IS</td><td align="center">TO-BE</td>
</tr>
<tr>
<td>

```kotlin
class RecommendationsTests {
    private val distanceCalculator = mock(DistanceCalculator::class.java)
    private val featuredDestinations = mock(FeaturedDestinations::class.java)
    
    private val recommendations = Recommendations(
        featuredDestinations::findCloseTo,
        distanceCalculator::distanceInMetersBetween
    )
}

private fun givenFeaturedDestinationsFor(
    location: Location,
    result: List<FeaturedDestination>
) {
    Mockito.`when`(featuredDestinations.findCloseTo(location))
        .thenReturn(result)
}
```
</td>
<td>

```kotlin
private val featuredDestinations = mutableMapOf<Location, List<FeaturedDestination>>()
    .withDefault { emptyList() }

private fun givenFeaturedDestinationsFor(
    location: Location,
    destinations: List<FeaturedDestination>
) {
    featuredDestinations[location] = destinations.toList()
}
```
</td>
</tr>
</table>

- 데이터에 집중하면 테스트 단순해짐 &rarr; 특히 조회 기능 테스트