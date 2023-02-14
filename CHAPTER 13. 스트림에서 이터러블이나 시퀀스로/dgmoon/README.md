# 자바에서 코틀린으로 ch.13-14

## ch13. 스트림에서 이터러블이나 시퀀스로

자바 스트림

- 자바 8부터 도입
- 지연 계산(중간연산은 아무런 일도 하지 않고 최종연산만이 값을 빨아낼 수 있는 파이프라인을 설치.
- 값을 빨아들이는 연산은 컬렉션이 작은 경우 스트림이 매우 느리다.
- 자바 스트림이 일반적인 컬렉션 변환, 지연 계산, 병렬 처리 등의 작업을 모두 염두에 두고 설계됐는데 이런 작업의 요구 조건이 모두 다름.
- 코틀린은 병렬연산을 구현하지 않고, 이터러블(컬렉션 변환, 축약용)과 시퀀스(지연 연산용), 두 가지 추상화를 제공한다.

코틀린 이터러블

- 코틀린은 컬렉션 연산을 정의하기 위해 Iterable에 대한 확장함수를 제공한다.

```kotlin
fun averageNonBlankLength(strings: List<String>): Double = 
(strings
	.filter { it.isNotBlank() } //filter는 Iterable에 대한 확장함수. Stream이 아니라 List를 반환.
	.map(String::length)
	.sum()
		/ strings.size.toDouble())
```

- 위 코드는 2개의 리스트(filter와 map 함수가 반환하는 List들)를 추가로 만든다. 이는 더 많은 메모리가 필요하기 때문에 문제가 될 수 있다. 또한, 정수 리스트를 반환하는 경우 박싱이 필요하므로 문제가 된다 자바 스트림 예제는 mapToInt(String::length)를 사용해 이런 문제를 해결했다. (박싱, 언박싱 피하기 위해 IntStream, LongStream, DoubleStream을 제공하는데 BooleanStream, CharStream은 제공하지 않음)

- 위 코틀린 코드는 컬렉션 크기가 크지 않으면 빠르게 작동한다
- 컬렉션이 크다면 코틀린에서는 시퀀스로 전환할 수 있다.

코틀린 시퀀스

- 시퀀스는 지연 계산을 제공한다.
- Sequence에 대한 map 연산은 Sequence를 반환한다.

```kotlin
fun averageNonBlankLength(strings: List<String>): Double =
	(strings
		.asSequence()
		.filter { it.isNotBlank() }
		.map(String::length)
		.sum()
			/ strings.size.toDouble())
```

*** Iterable<T>의 확장 함수와 Sequence<T>의 확장 함수 시그니처가 같지만 같은 유형의 연산이 아니다. 하지만, 비슷한 API를 제공하기 때문에 둘 간의 전환이 필요할 때 변경이 거의 필요 없다.

- 시퀀스를 사용한 코드에서는 중간 리스트 생성 비용이 들지 않는다. 하지만 원소 개수가 적다면 파이프라인을 만들고 실행하는 비용이 리스트를 생성하는 비용보다 더 비싸진다.
- 시퀀스는 박싱을 피하기 위해 Int를 반환 하는 함수를 파라미터로 받는 방식으로 박싱을 피한다. → 다른 시퀀스나 컬렉션을 반환하지 않아서 성능 향상.

```kotlin
fun averageNonBlankLength(strings: List<String>): Double =
	(strings
		.asSequence()
		.filter { it.isNotBlank() }
		.sumBy(String::length)
		/ strings.size. toDouble())
```

다중 이터레이션

- 자바 스트림에는 감춰진 상태가 있다는 문제가 있다. 모든 원소를 소비하면 다시 처음으로 돌아가 count를 통해 원소 개수를 셀 수는 없다. 파이프라인의 각 스트림은 파이프라인에서 자기 바로 앞에 있는 다른 스트림을 소비하기 때문이다.

```kotlin
fun averageNonBlankLength(strings: List<String>): Double =
	averageNonBlankLength(strings.asSequence())

fun averageNonBlankLength(strings: Sequence<String>) Double =
	(strings
	.filter { it.isNotBlank() }
	.sumBy(String::length)
		/ strings.count().toDouble())
```

- 코틀린에서 List 버전에서  Sequence 버전을 호출하면  IllegaIStateException를 발생시킨다.

```kotlin
fun averageNonBlankLength(strings: Iterator<String>): Double =
averageNonBlankLength(strings.asSequence())
// 이 함수를 호출하면 IllegalStateException 발생.
```

- strings.count()가 0을 반환하면 averageNonBlankLength가 항상 Infinity를 반환하게 된다. 이것은 이터레이션을 둘러싸는 시퀀스는 Sequence.constranOnce()로 제약해서 방지한다.
- 파일이나 네트워크 소켓처럼 외부 자원을 읽어서 처리해야하는 경우에는 이터레이션이 불가하다.
    
    ```kotlin
    fun averageNonBlankLength(strings: Sequence<String>): Double {
    	var count = 0
    	return (strings
    	.onEach { count++ }
    	.filter { it.isNotBlank() }
    	•sumBy(String::length)
    	/ count.toDouble())
    ```
    
- 맨마지막에 count를 쓰는 대신 첫 번째 터잍터레이션을 수행하면서 개수를 세는 방식으로 해결 가능하다.
- 유틸리티 클래스에 넣어 지역 변수를 변이시키며 문제를 해결할 수도 있다.

```kotlin
class CountingSequence<T>(
private val wrapped: Sequence<T>
) : Sequence<T> {
		var count = 0
		override fun iterator() =
		wrapped.onEach { count++ }.iterator()
	}

	fun averageNonBlankLength(strings: Sequence<String>): Double {
		val countingsequence = CountingSequence(strings)
		return (countingsequence
		.filter { it.isNotBlank() }
		•sumBy(String::length)
		/ countingsequence.count.toDouble())
}
```

- 스트림이나 시퀀스의 문제는 데이터 집합의 원소 개수를 하나하나  세서 전체 크기를 알아내는 과정은 그리 효율적이지 못하다.

 

스트림, 이터러블 시퀀스 중 선택하기

- 코틀린에서는 람다를 인자 목록 밖으로 뺄 수 있고,  it을 사용할 수 있으며, 확장함수를 사용하여 자바 스트림을 더 멋지게 개선할 수 있다.
- 지연 계산이 필요하면 Sequence, 아니면 Iterable
    - 지연계산이 필요한 경우는

대수적 변환

- 대수: 코드의 동작을 그대로 유지하면서 연산을 조작할 수 있는 규칙을 제공하는 코드.

```kotlin
fun averageNonBlankLength(strings: Sequence<String>): Double = 
	strings
	.map { if (it.isBlank()) 0 else it.length }
	.average()
```

- 위 코드는 수학적 재배열이지만 모든 연산이 계산일 경우에만 제대로 동작하기 때문에 위험하다.

```kotlin
fun averageNonBlankLength(strings: Sequence<String>): Double =
	strings.averageBy {
	if (it.isBlank()) 0 else it.length
	}
```

효율성을 위해 변이를 허용하여 위와 같이 리팩터링하였다.

- for문과 if 문으로 구성된 함수 → 스트림 filter 사용
- 초기 스트림 변환을 거치면 다음과 같이 된다.

```kotlin
public static double averageNonBlankLength(List<String> strings) {
	return strings
	.streamO
	.mapToInt(s -> s.isBlank() ? 0 : s.length())
	.average()
	.orElse(Double.Nan)
```

스트림에서 이터러블이나 시퀀스로 리팩터링하기

```kotlin
public double averageNumberOfEventsPerCompletedBooking(
	String timeRange
) {
	Stream<Map<String, Object» eventsForSuccessfulBookings =
		eventstore
			.queryAsStream("type=CompletedBooking&timerange=" + timeRange)
			.flatMap(event -> {
				String interactionld = (String) event.get("interactionld");
				return eventstore.queryAsStream("interactionId=” + interactionld);
			});
	Map<String, List<Map<String, Object»> bookingEventsBylnteractionld =
		eventsForSuccessfulBookings.collect(groupingBy(
			event -> (String) event.get("interactionld"))
		);
	var averageNumberOfEventsPerCompletedBooking =
		bookingEventsBylnteractionld
			.valuesO
			.stream()
			.mapToInt(List::size)
			.average();
		return averageNumberOfEventsPerCompletedBooking.orElse(Double.NaN);
```

- 위 코드는 성공적인 예약을 하기 위해 필요한 상호작용의 평균 횟수 계산 코드
- 자동 변환하면 스트림 코드에서는 문제점이 많이 생김
- 코틀린에서는 길이를 알 수 없는 입력에 대해 Sequence 사용, 메모리상의 데이터에 대해서는 Iterable로 처리하려 한다.
- Map.values가 컬렉션 연산 적용이 가능하다는 것을 알고 있으므로 .stream()을 제거 가능하다.

```kotlin
class MarketingAnalytics(
	private val eventstore: Eventstore
) {
	fun averageNumberOfEventsPerCompletedBooking(
		timeRange: String
	): Double = eventstore
		.queryAsSequence("type=CompletedBooking&timerange=$timeRange")
		. aUEventsInSamelnteractions()
		.groupBy(Event interactionld)
		.values
		.averageBy { it.size }
	private fun Sequence<Event>.allEventsInSameInteractions() =
		flatMap { event ->
		eventstore.queryAsSequence(
		"interactionld=${event.interactionld}"
		)
		}
}

inline fun <T> Collection<T>.averageBy(selector (T) -> Int): Double =
sumBy(selector) / size.toDouble()

fun Eventstore.queryAsSequence(query: String) =
this.queryAsStream(query).asSequence()
```
