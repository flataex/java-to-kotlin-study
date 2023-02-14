## ch.14 누적시키는 객체에서 변환으로

## 가변 객체와 부수 효과에 의존하는 자바 코드를 불변 값을 변환하는 코틀린 코드로 바꾸기

누적기 파리미터를 사용해 계산하기

- CostSummary: 비용 총합 계산
- CostSummaryCalculator: 비용을 선호하는 통화로 계산

```kotlin
val fx ExchangeRates = ...
val userCurrency = ...
val calculator = CostSummaryCalculator(userCurrency, fx) // CostSummaryCalculator 생성
fun costSummary(i Itinerary) CostSummary {
i.addCostsTo(calculator) // 비용을 계산기 쪽에 더하도록 한다.
return calculator.summarise()
}
```

```kotlin
data class Itinerary(
val id: Id<Itinerary>,
val route: Route,
val accommodations: List<Accommodation> = emptyList()
	fun addCostsTo(calculator: CostSummaryCalculator) {
		route.addCostsTo(calculator)
		accommodations.addCostsTo(calculator)
		}
	}

	fun Iterable<Accommodation.addCostsTo(calculator: CostSummaryCalculator) {
		forEach { a ->
			a.addCostsTo(calculator)
	}
}
```

- 위 코드는 가변 상태에 비용을 누적하기 위해 부수 효과를 사용한다. 이렇게 하면 도메인 모델에 있는 어떤 객체나 이 계산기를 사용해 비용 합계를 구할 수 있게 된다.
- 그러나 에일리어싱 오류(알기 어려운 이상한 원격 반응) 발생 가능성이 높아지고, 알고리즘 구현이 여기 저기 흩어져 있게 된다는 문제가 있다.
- CostSummary는 매번 다시 합계를 계산할 때 병목이며, 공유된 가변 컬렉션이므로 6.1 절의 ‘공유된 컬렉션 변경하지 말라’라는 원칙에 위배된다.
- 가변 상태를 공유해 수행하면 책임이 여러 클래스에 지저분하게 분산되는 경우가 흔하다.
- 동작주 명사: driver, baker 등과 같이 동사의 동작을 수행하는 사람이나 물건.
- 동작주 명사나 클래스가 저장한 데이터에서 다른 힌트를 얻을 수 있는 경우

불변 데이터에 작용하는 함수로 리팩터링하기 

한 번 더 해보자

발견한 추상화를 더 풍성하게 만들기

함수형 프로그래밍은 가변 상태를 제거하지 않으며, 대신 가변 상태를 런타임의 책임으로 만든다.
