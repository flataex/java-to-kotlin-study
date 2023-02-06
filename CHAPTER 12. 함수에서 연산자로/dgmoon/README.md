# ch12. 함수에서 연산자로

사용자 정의 연산자 추가

클래스에 대해 plus라는 이름의 연산자 메서드나 연산자 확장함수를 작성하면 + 연산자가 추가된다.
```kotlin
class Moneyy private constructor(
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

@JvmName 애너테이션을 사용하면 코틀린과 자바에서 메서드가 다른 이름으로 불리게 할 수 있다.


코틀린에서는 객체 생성과 함수 호출을 구분하지 않으며, 클래스 생성자 호출과 함수 호출의 문법이 같다.

클ㄹ래스에 팩터리 메서드가 여러 개 필요한 경우, 보통 클래스의 동반 객체가아니라 최상위 수준의 함수로 이들을 정의한다.
