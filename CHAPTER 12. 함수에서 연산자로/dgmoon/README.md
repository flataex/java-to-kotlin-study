# ch12. 함수에서 연산자로

지금까지는 항상 변환 대상 코드를 전형적인 코틀린 코드로 변환했지만, 이런 변환이 항상 가능한 것은 아니다. 코드 기반의 크기가 클수록 자바와 코틀린이 상당 기간 공존할 수밖에 없다.

코틀린에서는 사용자 정의 연산자를 추가할 수 있다.

클래스에 대해 plus라는 이름의 연산자 메서드나 연산자 확장함수를 작성하면 + 연산자가 추가된다. 
```kotlin
class Money private constructor(
    val amount: BigDecimal
    val currency: Currency
) {
    ...
    operator fun plus(that: Money): Money {
        require(currency == that.currency) {
            "cannot add Money values of different currencies"
        }
        return Money(amount.add(that.amount), currency)
    }
    ...
}
```

이렇게 하면 + 연산자로 Money의 값을 더할 수 있다.
```kotlin
val money1 = Money(BigDecimal.valueOf(10000), Currency.getInstance("KRW"))
val money2 = Money(BigDecimal.valueOf(10000), Currency.getInstance("KRW"))

println((money1 + money2).amount) // 20000
```

@JvmName 애너테이션을 사용하면 코틀린과 자바에서 메서드가 다른 이름으로 불리게 할 수 있다.
```kotlin
    @JvmName("add")
    operator fun plus(that: Money): Money {
        require(currency == that.currency) {
            "cannot add Money values of different currencies"
        }
        return Money(amount.add(that.amount), currency)
    }
```

인텔리제이의 변환 기능은 아직까지 이런 operator가 붙은 메서드 호출을 적절한 연산자 호출로 변환해 주지 않는다. 따라서, 책에서는 각 연산자에 해당하는 메서드를 하나씩 따로 리팩터링한다.
```kotlin
fun add(that: Money) = this + that // add 메서드는 메서드 호출을 연산자 호출로 변경한 뒤 다닝ㄹ식 형태로 바꿔줬다.

operator fun plus(that: Money): Money { // add 메서드에 해당하는 로직을 plus라는 이름의 메서드로 추출한다.
    require(currency == that.currency) {
        "cannot add Money values of different currencies"
    }
    return Money(amount.add(that.amount), currency)
} // add 메서드는 자바 코드를 위해, plus 메서드는 코틀린 코드를 위해 사용될 것이다.
```

아래 코드가 컴파일 될 수 있는 이유는 코틀린 표준 라이브러리에 자바 표준 라이브러리가 제공하는 클래스(BigInteger, BigDecimal, List\<T\>, Set\<T\> 등)에 대한 연산자를 정의하는 확장 함수가 들어 있기 때문이다.
```kotlin
operator fun plus(that: Money): Money {
    require(currency == that.currency) {
        "cannot add Money values of different currencies"
    }
    return Money(this.amount + that.amount, currency)// BigDecimal 타입인 amount의 add 메서드를 코틀린 표준 라이브러리가 이미 포함하고 있다.
}
```

코틀린에서는 객체 생성과 함수 호출을 구분하지 않으며, 클래스 생성자 호출과 함수 호출의 문법이 같다.

Money 클래스 처럼 팩터리 메서드가 여러 개 필요한 경우, 보통 클래스의 동반 객체가 아닌 최상위 함수로 이들을 정의한다.(Money.of()보다 moneyOf() 선호.)

최상위 함수 정의 시 생성자의 가시성을 private이 아닌 internal로 바꾸면 최상위 함수가 이 생성자를 호출 할 수 있다. 이렇게 하면 컴파일 모듈은 이 생성자를 호출할 수 있지만 컴파일 모듈 밖에서는 호출할 수 없다.

클래스에 팩터리 메서드가 여러 개 필요한 경우, 보통 클래스의 동반 객체가 아니라 최상위 수준의 함수로 이들을 정의한다.

자바에서는 internal 가시성이 없기 때문에 컴파일 시 공개 특성으로 번역되며, 이 때문에 자바 코드 작업시 실수로 객체를 생성하는 것이 허용된다. 때문에 최상위 함수는 매력적으로 보이지 않을 수 있다. 하지만 코틀린 연산자 오버로딩을 통해 이 문제를 해결할 수 있다.
```kotlin
@JvmStatic
fun of(amount: BigDecimal, currency: Currency) = // 메서드 추출 후 인라이닝 해준다.
    invoke(amount, currency)

operator fun invoke(amount: BigDecimal, currency: Currency) = // 연산자 호출 때와 마찬가지로 invoke란 이름으로 of 메서드의 로직을 추출한다. private 접근 제어자를 지워 공개 메서드로 바꿔준다.
    Money(
        amount. setScale(currency.defaultFractionDigits),
        currency
    )
```
