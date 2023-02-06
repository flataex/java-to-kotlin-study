# ch10. 함수에서 확장함수로

- 객체 지향 -> 메시지를 객체에 보내서 문제 해결
- 함수형 프로그래밍 -> 함수를 호출해서 문제 해결

함수형 프로그래밍의 경우 발견 가능성 매우 낮음
자바의 경우 확장성이 떨어지지만(객체나 동반 객체에 모아둠), 코틀린의 경우 확장 가능.(최상위 함수 사용.)

- 확장 함수를 실제로 다른 클래스의 비정적 메서드로 정의 할 수 있음

- 확장함수는 어떤 타입에 적용할 수 있는 연산을 확장할 수 있게 해 준다.

확장 함수 정의 방법
```kotlin
fun Customer.nameForMarketing() = "${familyName.upperCase()}, $givenName}"
```

호출 방법
```kotlin
val s = customer.nameForMarket()
```


- 수신 객체: 메시지를 받을 객체

확장 함수는 자신이 정의된 영역에 있는 일반 함수와 똑같은 권한을 가진다.(확장하는 클래스 비공개 멤버 접근 불가능)

확장 함수를 일반 함수의 참조에 대입할 수는 있다.

```kotlin
val methodReference: (Customer.() -> String) =
    Customer::fullName
val extensionFuctionReference: (customer.() -> String) =
    Customer::nameForMarekting

val methodFunctionReference: (Customer) -> String =
    methodReference
val extensionAsFunctionReference: (Customer) -> String =
    extensionFunctionReference
```
호출 방법
```kotlin
customer.methodReference()
customer.extensionFunctionReference()

methodReference(customer)
extensionFunctionReference(customer)
// 수신 객체를 사용하는(with-receiver) 참조로 호출: 수신 객체를 첫번째 인자로 사용하여 호출
```

일반 함수에 대한 참조를 수신 객체가 있는 것처럼 호출할 수는 없다.
```kotlin
// 호출 불가
// customer.methodReference(customer)
// customer.extensionFunctionReference(customer)
```

확장 프로퍼티: 프로퍼티(실제로는 메서드임)처럼 호출 할 수 있는 정적 함수.

```kotlin
val Customer.nameForMarketing get() = "${familyName.uppercase}, $givenName}"
```

확장 함수의 흔한 예는 한 타입에서 다른 타입으로 변환하는 연산을 들 수 있다.(책에서는 JsonNode -> Customer 변환을 예로 들었다.)

파라미터에서는 안전한 호출 연산자(?.)가 도움이 되지 않고, let, apply, also 등의 영역 함수(scoping function, 스코프 함수)가 도움이 된다.

let 함수: 수신객체를 람다의 파라미터로 바꿔준다.

```kotlin
val customer: Customer? = loggedInCustomer()
val greeting: String? customer?.let { greetingForCustomer(it) }
```

함수가 널이 될 수 있는 결과를 반환하고 그결과를 널이 아닌 파라미터를 받는 함수에 전달해야만 한다면 영역 함수가 꼬이기 시작하는데, 문제가 되는 파라미터를 확장함수의 수신 객체로 만들면 호출을 직접 연결해서 애플리케이션 로직이 들어 나게 할 수 있다.

메서드 호출은 null인 참조가 있을 때 전달할 대상이 없기 때문에 메시지를 보낼 수 없다.(NPE 발생.) 하지만 확장함수의 수신객체가 널이 될 수있는 타입일 경우 호출될 수 있다.

- tip: 널이 될 있는 타입의 확장함수를 작성한다면 수신 객체가 널일 때 null을 반환하는 코드를 작성해서는 안된다. 널을 반환하는 확장 함수가 필요하다면 확장을 널이 될 수 없는 타입의 확장으로 정의하고 확장 호출 시 안전한 호출 연산자(?.)를 사용하라.

확장 함수도 제네릭 파라미터를 포함할 수 있다.

```kotlin
fun <T> T.printed(): T = this.also(::prinln)
```

```kotlin
val marketingLength = jsonNode.toCustomer().nameForMarketing().length
```

확장 함수를 클래스 정의 내부에 넣을 수도 있으며, 이렇게 하면 정의된 클래스의 멤버에 접근하면서 다른 타입을 확장할 수 있게 된다.

결론: 확장 함수를 사용하며 더 쉽게 발견할 수 있고, 더 잘 이해할 수 있으며, 더 잘 유지보수 할 수 있다.
