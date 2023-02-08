# 함수에서 확장 함수로

## 함수와 메서드
- 객체지향 프로그래밍은 객체 간의 메세지에 집중하여 값을 주고 받고 객체의 상태를 변경하는 방식의 프로그래밍 기법입니다.
- 함수형 프로그래밍은 값을 사용해 함수를 호출하는 방식으로 문제를 해결하는 프로그래밍 기법입니다.
- 함수형 프로그래밍은 함수가 타입 위에 정의되지 않고 함수의 파라미터와 결과가 타입을 소유하는 방식으로 사용됩니다.
- 기능에 대한 발견 가능성은 클래스 단위로 묶는 객체지향 접근 방식이 함수형 접근 방식보다 더 좋습니다.
    - 함수형은 인자로 받는 형태이기 때문에 어떠한 기능에 대한 함수가 실제로 존재하는지 상대적으로 찾기 어렵습니다.
- 확장성은 인자로 값을 받는 함수형 접근 방식이 객체지향 접근 방식보다 더 뛰어납니다.
    - 객체지향 접근 방식은 객체를 소유하지 않는다면 기능을 확장하기 어렵기 때문에 함수형 접근 방식을 사용하여 기능을 확장해야 합니다.
- 코틀린에서는 확장함수를 통해 함수형의 확장성을 살리고 발견 가능성을 개선할 수 있습니다.

## 확장 함수
- 코틀린의 확장함수는 어떤 타입의 적용할 수 있는 연산을 확장해주는 함수입니다.
    - `fun 타입.이름 = 코드` 와 같은 형식으로 확장 함수를 정의할 수 있습니다.
- 자바에서는 static 메서드로 인식되기 때문에 코틀린보다는 활용성이 떨어집니다.
- 코틀린에서는 자바와 같이 static import를 활용하여 확장함수를 호출할 수 없습니다.
- 확장 함수는 자신이 확장하는 클래스의 비공개 멤버에 대한 특별한 접근 권한을 부여받지 않습니다.
    - 정의된 영역과 똑같은 권한을 가집니다.

## 확장 함수의 타입과 함수의 타입
- 확장 함수를 일반 함수 처럼 호출할 수는 없지만 다음과 같이 확장 함수를 일반 함수의 참조에 대입할 수는 있습니다.

    ```kotlin
    val methodReference: (Customer.() -> String) = Customer::fullName
    val extensionFunctionReference: (Customer.() -> String) = 
    	Customer::nameForMarketing
    
    val methodAsFunctionReference: (Customer) -> String = 
    	methodReference
    val extensionAsFunctionReference: (Customer) -> String =
    	extensionFunctionReference
    ```

- 확장 함수에 대한 참조인 경우 마치 수신 객체가 첫 번째 인자인 것 처럼 수신 객체를 사용하는 (with-receiver) 참조를 사용해 호출할 수도 있습니다.

    ```kotlin
    methodReference(customer)
    extensionFunctionReference(customer)
    ```

- 하지만 일반 함수에 대한 참조를 수신 객체가 있는 것 처럼 호출할 수는 없습니다.

    ```kotlin
    customer.methodAsFunctionReference()
    customer.extensionAsFunctionReference()
    ```

## 확장 프로퍼티
- 코틀린은 프로퍼티도 확장하여 확장 프로퍼티로 사용할 수 있습니다.
- 코틀린 프로퍼티 접근자는 실제로는 메서드 호출입니다.
- 확장 프로퍼티는 실제 클래스에 필드를 추가할 수 없으므로 확장 프로퍼티에 데이터를 저장할 수는 없고, 값을 계산해 돌려줄 수만 있습니다.
- 확장의 경우 코틀린은 컴파일 시점에 수신 객체의 정적 타입을 바탕으로 어떤 함수를 호출할지 결정하게 됩니다.
    - 다형적으로 확장 함수를 사용해야 할 필요가 있다면 보통은 확장 함수에서 다형적인 메서드를 호출하는 방식으로 이런 필요를 만족시킵니다.
## 변환
- 확장함수는 A를 B로 변환하는 기능을 구현할때도 많이 사용합니다.
- 변환함수의 네이밍은 문장으로 읽히도록 자연스러운 것이 좋습니다.
- 코틀린에서는 확장함수를 통해 체이닝을 통해 합성을 할 수 있어 가독성적인 측면에서 훨씬 뛰어납니다.

    ```kotlin
    // java
    var marketingLength = nameForMarketing(customerFrom(node)).length();
    // kotlin
    val marketingLength = jsonNode.toCustomer().nameForMarketing().length
    ```
## 널이 될 수 있는 파라미터
- 널이 될 수 있는 파라미터를 사용하는 경우 스코프 함수가 도움이 됩니다.

    ```kotlin
    val customer: Customer? = loggedIncustomer()
    val greeting: String? = customer?.let { greetingForCustomer(it) }
    ```

- let과 확장함수를 사용하면 애플리케이션 로직을 깔끔하게 작성할 수 있습니다.

    ```kotlin
    // 확장함수 X, let X
    val greeting: String? = when(customer) {
    	null -> null
    	else -> greetingForCustomer(customer)
    }
    // 확장함수 X, let O
    val reminder: String? = customer
    	?.let { nextTripForCustomer(it) }
    	?.let { timeUntilDepartureOfTrip(it, currentTime) }
    	?.let { durationToUserFriendlyText(it) }
    // 확장함수 O, let O
    val reminder: String? = customer
    	?.nextTrip()
    	?.timeUntilDeparture(currentTime)
    	?.toUserFriendlyText()
    	?.plus(" until your next trip!")
    ```

- 확장함수를 사용할때 다음과 같은 장점을 얻을 수 있습니다.
    - 선택적인 데이터 처리가 더 쉬워 로직을 더 쉽게 작성할 수 있습니다.
    - 확장 함수를 사용하면 함수의 이름을 더 간결하게 작성할 수 있습니다.
## 널이 될 수 있는 수신 객체
- 메서드 호출과 함수 호출의 null 참조 취급 방법은 다릅니다.
    - 메서드 호출은 null에 메세지를 전달할 수 없으므로 NPE가 발생합니다.

        ```kotlin
        var a = null;
        a.getName() // null 이므로 NPE 발생
        ```

      단 null인 파라미터는 가질 수 있습니다. (널을 사용해 하는 일은 알 수 없지만 널 파라미터가 있다고 해서 실행 시점에 호출할 함수를 찾지 못하는 일은 없습니다.)

    - 확장 함수 호출은 대상이 null이어도 호출 가능합니다.

        ```kotlin
        // 불가능
        anObject.method()
        // 가능
        anObject.extensionFunction()
        ```

- 확장함수를 사용하면 널이 될 수 있는 타입에 대해 `?.` 라는 잡음을 발생시키지 않으면서 함수를 호출할 수 있습니다.

    ```kotlin
    val reminder String? = customer.nextTrip().reminderAt(currentTime)
    ```

  위와 같이 작성할 경우 다음과 같은 단점도 존재합니다.

    - 함수의 널 가능성 흐름을 알아보기 어려워집니다.
    - 여전히 널 가능성을 내포하고 있다는 단점이 존재합니다.
- 널이 될 수 있는 타입의 확장함수를 작성한다면 수신 객체가 널일때 null을 반환하는 코드를 작성해서는 안됩니다.
    - 널을 반환하는 확장 함수가 필요하다면 확장을 널이 될 수 없는 타입의 확장으로 정의하고 확장 호출 시 안전한 호출 연산자(`?.`)를 사용해야 합니다.
## 제네릭스
- 일반 함수와 마찬가지로 확장도 제네릭 파라미터를 포함할 수 있습니다.
- 제네릭을 사용하지 않고 값을 특정 타입으로 지정하면 (Any와 같은) 활용성이 떨어질 수 있습니다.
- 제네릭을 사용하여 어떤 타입이 들어올 때만 동작하는 확장함수를 정의할 수 있습니다.

    ```kotlin
    fun Iterable<Customer>.familyNames(): Set<String> = 
    	this.map(Customer::familyName).toSet()
    ```
## 확장 함수를 메서드로 정의하기
- 확장함수를 메서드로 사용하는 경우는 클래스에 멤버에 접근하여 추가적인 작업이 필요할 경우 클래스 내부에 확장함수를 선언하는 방식을 사용합니다.

    ```kotlin
    class JsonWriter(
    	private val objectMapper: ObjectMapper,
    ) {
    	fun Customer.toJson(): JsonNode = objectMapper.valueToTree(this)
    }
    ```

  위와 같은 기법은 어떤 수신 객체가 적용될지 알기 힘들기 때문에 너무 자주 사용하지 않는 것이 좋습니다. (단, 사용으로 얻는 이점이 크다면 사용하는 것이 좋습니다.)
## 확장 함수로 리팩터링 하기
- 인텔리제이의 자바 → 코틀린 변환기능을 사용할 때 자동 타입 변환이 잘 이루어지지 않기 때문에(int → double 같이) 주의해서 사용해야 합니다.
- 확장함수를 사용함으로써 코드의 흐름이 하나로 진행되어 읽기 편하고 동작들을 묶어 어떠한 동작을 표현하는지 쉽게 표현할 수 있게 해줍니다