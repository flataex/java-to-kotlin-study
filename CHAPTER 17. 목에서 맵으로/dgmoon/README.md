## ch.17 목에서 맵으로

목 프레임워크가 인터페이스 구현뿐만 아니라 예상되는 메서드 호출과 호출시 반환해야하는 내용을 기술해준다는 장점이 있지만, 목을 사용하면서 마지막 예상 반환 값이 장황해져 테스트를 망친다는 관점이 있다.

### 목을 맵으로 대체하기

먼저, 코틀린이 함수타입을 더 잘 지원하기 때문에 테스트를 코틀린으로 변환하고 

```kotlin
private fun givenFeaturedDestinationsFor(
	location: Location,
	result: List<FeaturedDestination>
) {
	Mockito.`when`(featuredDestinations.findCloseTo(location))
		.thenReturn(result)
}
```

목을 제거하기 위해 맵 데이터 구조(키: 파라미터, 값: 리턴 값)를 사용한다. 

```kotlin
private val featuredDestinations =
	mutableMapOf〈Location, List<FeaturedDestination»()
		.withDefault { emptyList() }

private fun givenFeaturedDestinationsFor(
	location: Location,
	destinations: List<FeaturedDestination>
) {
	featuredDestinations[location] = destinations.toList()
}
```

### 그렇지만 실제로 목에서 벗어났는가

얼핏 보기에 목 프레임워크를 흉내내기만 한 것처럼 보이지만, 테스트를 좀 더 단순하게 만들어준다.

TDD는 데이터 흐름으로 살펴 보면 나았을 설계를 객체 간의 상호작용으로 표현함으로써 설계상의 문제를 가리게 되는 경향이 있다. 데이터에 집중하면 테스트가 단순해지며, 특히 값을 읽기만 하는 경우 더 그렇다.
