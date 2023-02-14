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

- 위 코드는 계산기의 가변 상태에 비용을 누적하기 위해 부수 효과를 사용한다. 이렇게 하면 도메인 모델에 있는 어떤 객체나 이 계산기를 사용해 비용 합계를 구할 수 있게 된다.
- 그러나 에일리어싱 오류(소스 코드를 보고 바로 이해하기 어려운 이상한 원격 반응) 발생 가능성이 높아지고, 알고리즘 구현이 여기 저기 흩어져 있게 된다는 문제가 있다.
- CostSummary는 매번 다시 합계를 계산할 때 병목이며, 공유된 가변 컬렉션이므로 6.1 절의 ‘공유된 컬렉션 변경하지 말라’라는 원칙에 위배된다.

- 가변 상태를 공유해 수행하면 책임이 여러 클래스에 지저분하게 분산되는 경우가 흔하다.
- 얽혀있는 책임을 풀기 위해, 동작주 명사(driver, baker 등과 같이 동사의 동작을 수행하는 사람이나 물건.)가 우리의 최종 목표에 대한 힌트를 주기도 한다.

불변 데이터에 작용하는 함수로 리팩터링하기 

```kotlin
class CostSummaryCalculator(
private val userCurrency: Currency,
private val exchangeRates: ExchangeRates
) {
	private val currencyTotals = mutableMapOfcCurrency, Money>()
	fun addCost(cost: Money) {
currencyTotals.merge(cost.currency, cost. Money：:add)
}
	fun summariseO： CostSummary {
		val totals = ArrayList(currencyTotals.values)
		totals.sortWith(comparing { m: Money -> m.currency.currencyCode })
		val summary = CostSummary(userCurrency)
		for (total in totals) {
		summary.addLine(exchangeRates.convert(totab userCurrency))
		}
		return summary
	}

	fun reset() {
	currencyTotals.clear()
	}
}
```

- 위 코드는 자동 변환 결과이며, 여기서부터 불안성을 제거하려고 한다. 제자리 정렬을 제일 먼저 고친다. 그리고 객체 생성→ 초기화 단계 호출 → apply 함수 패턴으로 고친다.

```kotlin
fun summarise(): CostSummary {
		val totals = currencyTotals.values.sortedBy {
		it.currency.currencyCode
	}
	val summary = CostSummary(userCurrency).apply {
		for (total in totals) {
			addLine(exchangeRates.convert(total, userCurrency))
		}
	}
	return summary
}
```

- 일단 가변성을 넣고 나면 나중에 제거가 힘든 경우가 많은데, 이럴 경우 fold가 도움이 된다.

```kotlin
class CostSummary(
	userCurrency: Currency,
	val lines: List<CurrencyConversion>
	) {
	val total = lines
	.map { it.toMoney }
	.fold(Money.of(0, userCurrency), Money::add)
}
```

- CostSummary 클래스를 데이터 클래스로 바꾸고 책임을 하나로 만든다.

```kotlin
class CostSummary(userCurrency: Currency) {
	private val _lines = mutableListOf<CurrencyConversion>()
	
	var total: Money = Money.of(0, userCurrency)
	private set
	
	val lines: List<CurrencyConversion>
		get() = _lines.toList()
	
	fun addLine(line: Currencyconversion) {
		.lines.add(line)
		total += line.toMoney
	}
}
```

발견한 추상화를 더 풍성하게 만들기

CostSummaryCalculator는 이제 단순히 비용 합계용 계산기가 아니라 고객별로 가격을 계산 할 때 필요한 문맥을 저장한다. 따라서 클래스 이름도 새로 발견된 책임을 반영할 필요가 있다. (PricingContext)

함수형 프로그래밍은 가변 상태를 제거하지 않으며, 대신 가변 상태를 런타임의 책임으로 만든다.
