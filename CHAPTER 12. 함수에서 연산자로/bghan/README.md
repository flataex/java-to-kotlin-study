# 함수에서 연산자로

## 12.1 토대가 되는 클래스: Money

```kotlin

import java.math.BigDecimal
import java.util.Currency

class Money private constructor(
    val amount: BigDecimal,
    val currency: Currency
) {
    override fun equals(other: Any?) =
        this === other ||
                other is Money &&
                amount == other.amount &&
                currency == other.currency
    
    override fun hashCode() =
        Objects.hash(amount, currency)
    
    override fun toString() =
        amount.toString() + " " + currency.currencyCode
    
    fun add(that: Money): Money {
        require(currency == that.currency) {
            "cannot add Money values of different currencies"
        }
        return Money(amount.add(that.amount), currency)
    }
    
    companion object {
        @JvmStatic
        fun of(amount: BigDecimal, currency: Currency) = Money(
            amount.setScale(currency.defaultFractionDigits),
            currency
        )
    }
}
```

#### [코틀린 동등/동일 연산자](https://wooooooak.github.io/kotlin/2019/02/24/kotiln_%EB%8F%99%EB%93%B1%EC%84%B1%EC%97%B0%EC%82%B0/) && [type check is 연산자](https://altongmon.tistory.com/601)

```kotlin
val a: String = "hi"
val b: String = "hi"

println(a == b) // true
print(a === b) // true 주소값 비교
```

```kotlin
val str = "str"
println(str is String) // true
```

## 12.2 사용자 정의 연산자 추가

#### [코틀린 연산자 오버로딩](https://thdev.tech/kotlin/2018/04/01/Kotlin-Operator-Overloading/)

<table>
<tr>
<td align="center">v1</td><td align="center">v2</td>
</tr>
<tr>
<td>

```kotlin
@JvmName("add")
fun add(that: Money): Money {
    require(currency == that.currency) {
        "cannot add Money values of different currencies"
    }
    return Money(amount.add(that.amount), currency)
}
```
</td>

<td>

```kotlin
@JvmName("add")
operator fun plus(that: Money): Money {
    require(currency == that.currency) {
        "cannot add Money values of different currencies"
    }
    return Money(amount.add(that.amount), currency)
}
```
</td>
</tr>
</table>

## 12.3 기존 코틀린 코드에서 정의한 연산자 호출하기

```kotlin
fun add(that: Money): Money {
    return this.plus(that)
}

// 위와 동일
fun add(that: Money): Money {
    return this + that
}

fun add(that: Money) = this + that
```

## 12.4 기존 자바 클래스를 위한 연산자

> BigDecimal.add 메서드를 + 연산자로 대치

```kotlin
operator fun plus(that: Money): Money {
    require(currency == that.currency) {
        "cannot add Money values of different currencies"
    }
    return Money(this.amount + that.amount. currency)
}
```

## 12.5 값을 표현하는 관습

#### 코틀린에서의 인스턴스 생성 convention
> Money(...), moneyOf(...), Money.of(...) x ex) Money 인스턴스 생성

`Money` vs `moneyOf` = `Money`
- 컴파일 이후 코틀린 클래스 내부 특성 &rarr; 공개 특성
- 내부 가시성으로 선언된 코틀린 &rarr; 자바 컴파일러와 JVM 입장에선 공개 가시성
- 자바 코드로 작업할 때 실수로 잘못된 Money 생성 허용 &rarr; **`moneyOf` 지양**

```kotlin

import java.math.BigDecimal

val currently = Money.of(BigDecimal("9.99"), GBP)
val proposal = Money(BigDecimal("9.99"), GBP) 

// 위와 동일
val proposal = Money.Companion.invoke(BigDecimal("9.99"), GBP)
```

#### `of` 메서드의 2가지 책임

1. Money 값을 생성하면서 클래스 불변 조건 강제
2. 값을 표현하는 객체를 생성하는 convention

> of &rarr; invoke로 바꾸면 2번 책임 망침

<table>
<tr>
<td align="center">v1</td><td align="center">v2</td><td align="center">v3</td>
</tr>
<tr>

<td>

```kotlin
import java.math.BigDecimal
import java.util.Currency

class Money private constructor(
    val amount: BigDecimal,
    val currency: Currency
) {
    companion object {
        @JvmStatic
        fun of(amount: BigDecimal, currency: Currency) =
            invoke(amount, currency)
        
        private fun invoke(amount: BigDecimal, currency: Currency) =
            Money(
                amount.setScale(currency.defaultFractionDigits),
                currency
            )
    }
}
```
</td>

<td>

```kotlin
import java.math.BigDecimal
import java.time.temporal.TemporalAmount
import java.util.Currency

@JvmStatic
fun of(amount: BigDecimal, currency: Currency) =
    invoke(amount, currency)

operator fun invoke(amount: BigDecimal, currency: Currency) =
    Money(
        amount.setScale(currency.defaultFractionDigits),
        currency
    )
```
</td>

<td>

```kotlin
import java.math.BigDecimal
import java.util.Currency

@JvmStatic
fun of(amount: BigDecimal, currency: Currency) =
    this(amount, currency)
```
</td>
</tr>
</table>

<table>

<tr>
<td align="center">v1</td><td align="center">v2</td>
</tr>

<tr>

<td>

```kotlin
import java.math.BigDecimal
import java.util.Currency

interface ExchangeRates {
    fun rate(fromCurrency: Currency, toCurrency: Currency): BigDecimal 
    
    @JvmDefault
    fun convert(fromMoney: Money, toCurrency: Currency): CurrencyConversion {
        val rate = rate(fromMoney.currency, toCurrency)
        val toAmount = fromMoney.amount * rate
        val toMoney = Money.of(toAmount, toCurrency)
        
        return CurrencyConversion(fromMoney, toMoney)
    }
}
```
</td>

<td>

```kotlin
import java.math.BigDecimal
import java.util.Currency

interface ExchangeRates {
    fun rate(fromCurrency: Currency, toCurrency: Currency): BigDecimal 
    
    @JvmDefault
    fun convert(fromMoney: Money, toCurrency: Currency): CurrencyConversion {
        val rate = rate(fromMoney.currency, toCurrency)
        val toAmount = fromMoney.amount * rate
        val toMoney = Money(toAmount, toCurrency) // 인라이닝
        
        return CurrencyConversion(fromMoney, toMoney)
    }
}
```
</td>
</tr>
</table>