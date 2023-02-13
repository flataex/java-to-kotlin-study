# 누적시키는 객체에서 변환으로

## 14.1 누적기 파라미터를 사용해 계산하기

```kotlin
data class Itinerary(
    val id: Id<Itinerary>,
    val route: Route,
    val accommodations: List<Accommodation> = emptyList()
) {
    fun addCostsTo(calculator: CostSummaryCalculator) {
        route.addCostsTo(calculator)
        accommodations.addCostsTo(calculator)
    }
}

fun Iterable<Accommodation>.addCostsTo(calculator: CostSummaryCalculator) {
    forEach { a -> 
        a.addCostsTo(calculator)
    }
}
```

| 장점                                | 단점                                    |
|:----------------------------------|:--------------------------------------|
| 도메인 모델에 있는 어떤 객체를 사용해 비용 합계 계산 가능 | 코드를 보고 즉시 알기 어려운 aliasing error 발생 가능 |

<table>
<tr>
<td>

```kotlin
data class CurrencyConversion(
    val fromMoney: Money,
    val toMoney: Money
)
```
</td>
<td>

```java
import java.util.ArrayList;
import java.util.Currency;

public class CostSummary {
    private final List<CurrencyConversion> lines = new ArrayList<>();
    private Money total;

    public CostSummary(Currency currency) {
        this.total = Money.of(0, currency)
    }
    
    public void addLine(CurrencyConversion line) {
        lines.add(line);
        total = total.add(line.getToMoney());
    }
    
    // "공유된 collection은 변경하지 말라" 에 위배되어 List 복사
    public List<CurrencyConversion> getLines() {
        return List.copyOf(lines);
    }
    
    public Money getTotal() {
        return total;
    }
}
```
</td>
<td>

```java
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;

public class CostSummaryCalculator {
    private final Currency userCurrency;
    private final ExchangeRates exchangeRates;
    private final Map<Currency, Money> currencyTotals = new HashMap<>();

    public CostSummaryCalculator(
            Currency userCurrency,
            ExchangeRates exchangeRates
    ) {
        this.userCurrency = userCurrency;
        this.exchangeRates = exchangeRates;
    }

    public void addCost(Money cost) {
        currencyTotals.merge(cost.getCurrency(), cost, Money::add);
    }

    public CostSummary summarise() {
        val totals = new ArrayList<>(currencyTotals.vales());
        totals.sort(comparing(m -> m.getCurrency().getCurrencyCode()));
        
        CostSummary summary = new CostSummary(userCurrency);
        
        for(var total : totals) {
            summary.addLine(exchangeRates.convert(total, userCurrency));
        }
        
        return summary;
    }
    
    public void reset() {
        currencyTotals.clear();
    }
}
```
</td>
</tr>
</table>

#### CostSummary 계산 책임

> 책임이 두 가지 클래스에 분산되어 있음 &rarr; 동작과 계산을 분리
> 아무런 데이터도 소유하지 않으며 동작에 필요한 데이터를 임시 저장만 할뿐

## 14.2 불변 데이터에 작용하는 함수로 리팩터링하기

<table>
<tr>
<td align="center">v1</td><td align="center">v2</td>
</tr>
<tr>
<td>

```kotlin
import java.util.Currency

class CostSummary(userCurrency: Currency) {
    private val _lines = mutableListOf<CurrencyConversion>()
    
    val total: MOney = Money.of(0, userCurrency)
        private set
    
    val lines: List<CurrencyConversion>
        get() = _lines.toList()
    
    fun addLine(line: CurrencyConversion) {
        _lines.add(line)
        total += line.toMoney
    }
}
```
</td>
<td>

```kotlin
import java.util.Currency

class CostSummaryCalculator(
    private val userCurrency: Currency,
    private val exchangeRates: ExchangeRates
) {
    private val currencyTotals = mutableMapOf<Currency, Money>()
    
    fun addCost(cost: Money) {
        currencyTotals.merge(cost.currency, cost, Money::add)
    }
    
    fun summarise(): CostSummary {
        val totals = ArrayList(currencyTotals.values)
        totals.sortWith(comparing { m: Money -> m.currency.currencyCode })
        
        val summary = CostSummary(userCurrency)
        
        for (total in totals) {
            summary.addLine(exchangeRates.convert(total, userCurrency))
        }
        
        return summary
    }
    
    fun reset() {
        currencyTotals.clear()
    }
}
```
</td>
</tr>
<tr>
<td>

```kotlin
fun summarise(): CostSummary {
    val totals = currencyTotals.values.sortedBy {
        it.currency.currencyCode
    }
    
    val summary = CostSummary(userCurrency)
    
    for (total in totals) {
        summary.addLine(exchangeRates.convert(total, userCuurency))
    }
    
    return summary
}
```
</td>
<td>

```kotlin
fun summarise(): CostSummary {
    val totals = currencyTotals.values.sortedBy {
        it.currency.currencyCode
    }
    
    val summary = CostSummary(userCurency).apply {
        for (total in totals) {
            addLine(exchangeRates.convert(total, userCurrency))
        }
    }
    
    return summary
}
```
</td>
</tr>
<tr>
<td colspan="2">

```kotlin
fun summarise(): CostSummary {
    val conversions = currencyTotals.values.sortedBy {
        it.currency.currencyCode
    }.map { exchangeRates.convert(it, userCurrency)}
    
    return CostSummary(userCurrency).apply {
        conversions.forEach(this::addLine)
    }
}
```
</td>
</tr>
<tr>
<td colspan="2">

```kotlin

import java.util.Currency

class CostSummary(userCurrency: Currency) {
    private val _lines = mutableListOf<CurrencyConversion>()
    
    val total: Money = Money.of(0, userCurrency)
        private set
    
    val lines: List<CurrencyConversion>
        get() = _lines.toList()
    
    fun addLine(line: CurrencyConversion) {
        _lines.add(line)
        total += line.toMoney
    }
}

val total = lines
    .map { it.toMoney }
    .fold(Money.of(0, userCurrency), Money::add)
```
</td>
</tr>
</table>

#### [kotlin reduce()와 fold()](https://blog.leocat.kr/notes/2020/03/09/kotlin-reduce-and-fold)

> reduce()는 초기값이 없이 첫번째 요소(element)로 시작<br>
> fold()는 지정해 준 초기값으로 시작

```kotlin
val numbers = listOf(7, 4, 8, 1, 9)

val sum = numbers.reduce { total, num -> total + num }
println("reduced: $sum") // reduced: 29

val sumFromTen = numbers.fold(10) { total, num -> total + num }
println("folded: $sumFromTen") // folded: 39
```

## 14.3 한 번 더 해보자

```kotlin

import java.util.Currency

class CostSummaryCalculator(
    private val userCurrency: Currency,
    private val exchangeRates: ExchangeRates
) {
    private val currencyTotals = mutableMapOf<Currency, Money>()
    
    fun addCost(cost: Money) {
        currencyTotals.merge(cost.curency, cost, Money::add)
    }
    
    fun summarise(): CostSummary {
        val lines = currencyTotals.values
            .sortedBy { it.currency.currencyCode }
            .map { exchangeRates.convert(it, userCurrency) }
        
        val total = lines
            .map { it.toMoney }
            .fold(Money.of(0, userCurrency), Money::add)
        
        return CostSummary(lines, total)
    }
    
    // 위와 동일
    fun summarise(costs: Iterable<Money>): CostSummary {
        val delegate = CostSummaryCalculator(userCurrency, exchangeRates)
        costs.forEach(delegate::addCost)
        
        return delegate.summarise()
    }
    
    fun reset() {
        currencyTotals.clear()
    }
}
```

## 14.4 발견한 추상화를 더 풍성하게 만들기

```kotlin

import java.util.Currency

class PricingContext(
    private val userCurrency: Currency,
    private val exchangeRates: ExchangeRates
) {
    fun toUserCurrency(money: Money) = 
        exchangeRates.convert(money, userCurrency)
    
    fun summarise(costs: Iterable<Money>): CostSummary {
        val currencyTotals: List<Money> = costs
            .groupBy { it.currency }
            .values
            .map {
                it.sumOrNull() ?: error("Unexpected empty list")
            }
        
        val lines: List<CurrencyConversion> = currencyTotals
            .sortedBy { it.currency.currencyCode }
            .map(::toUserCurrency)
        
        val total = lines
            .map { it.toMoney }
            .sum(userCurrency)
        
        return CostSummary(lines, total)
    }
}
```

#### 결론

```kotlin
val fx: ExchangeRates = ...
val userCurrency = ...
val pricing = PricingContext(userCurrency, fx)

fun costSummary(i: Itinerary) = pricing.summarise(i.costs())
```