## 사용자 정의 연산자 (operator overloading)

코틀린에서는 아래와 같이 연산자를 오버로딩해 사용할 수 있다.

```kt
fun add(that: Money) = this + that

operator fun plus(that: Money): Money { 
    require(currency == that.currency) {
        "cannot add Money values of different currencies"
        }
    return Money(amount.add(that.amount), currency) 
}
```

<br>

클래스에 팩터리 메서드가 여러 개 필요한 경우, companion object보다 최상위 함수의 수준으로 이들을 정의한다. <br>
하지만 private 생성자를 최상위 함수가 호출할 수는 없기에, 캡슐화 하기엔 무리가 있다. <br>

internal 생성자를 사용하면 호출이 가능하지만, 자바와 JVM에는 내부 가시성 개념이 없어서 잘못된 Money 값을 생성할 수 있다. <br>

이런 경우 invoke operator를 overloading하여 사용할 수 있다. <br>
이렇게 하면 자바에서는 자바의 관습대로, 코틀린에서는 코틀린 관습대로 사용할 수 있다.

```kt
    companion object {
        @JvmStatic
        fun of(amount: BigDecimal, currency: Currency) =
            this(amount, currency)

        operator fun invoke(amount: BigDecimal, currency: Currency) =
            Money(
                amount.setScale(currency.defaultFractionDigits),
                currency
            )
    }
```
