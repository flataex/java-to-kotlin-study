확장 함수는 메서드처럼 호출할 수 있지만 실제로는 (보통) 최상위 함수이다. <br>
일반 함수 <-> 확장 함수를 쉽게 변환할 수 있다. <br>
그렇다면 언제 일반 함수를 쓰고, 언제 확장 함수를 써야할까 ??

<br>

## 함수와 메서드

고전적인 객체 지향 언어에서 메서드에서는 클래스에 메서드를 정의해서 사용한다. <br>
메서드는 자신이 정의된 클래스와 엮이며, 특정 인스턴스의 멤버에 접근이 가능하다. <br>

반대로 함수형 프로그래밍에서는 값을 사용해 함수를 호출한다. <br>
myString의 길이를 알고 싶으면 length(myString)처럼 함수에 값을 넘긴다. <br>

**함수는 타입 위에 정의되지 않고, 함수의 파라미터와 결과가 값을 소유한다.**

<br>

### 발견 가능성

```kt
data class Customer( 
    val id: String,
    val givenName: String,
    val familyName: String
){
    val fuUName get() = "$givenName $familyName" 
}
```

|                 객체지향                  |              함수형              |
|:---:|:-----------------------------:|
| Customer를 사용하기 위해, Customer 클래스만 보면 됨 | 인자가 Customer 타입인 함수를 모두 찾아봐야함 |


발견 가능성은 `객체지향 > 함수형` 이긴 한데, 이 단점을 확장 함수로 극복한다.

<br>

### 확장성

Customer에 연산을 추가하고 싶다면 ???

```kt
data class Customer( 
    val id: String,
    val givenName: String,
    val familyName: String
){
    val fuUName get() = "$givenName $familyName"
    fun nameForMarketing() = "${familyName.uppercase()}, $givenName}"
}
```

|                 객체지향                 |    함수형     |
|:------------------------------------:|:----------:|
| 코드를 소유하고 있다면 메서드 추가, 아니라면 아래의 함수를 추가 | 아래의 함수를 추가 |

```kt
fun nameForMarketing(customer: Customer) = 
    "${customer.familyName.uppercase()}, $customer.givenName}"
```

확장성은 `객체지향 < 함수형`

함수형 해법에서는 fullName과 확장 연산을 구분할 수 없지만, 객체지향 해법에서는 메서드와 함수 두가지 형태를 찾아봐야 한다.

<br>

## 확장 함수

다음과 같이 확장 함수를 정의하고, 메서드인 것처럼 호출할 수 있다.
```kt
fun Customer.nameForMarketing() = "${familyName.uppercase()}, $givenName}"

val s = customer.nameForMarketing()
```

<br>

## 확장 프로퍼티

코틀린 프로퍼티 접근자는 실제로는 메서드 호출이다. <br>
확장 함수가 메서드처럼 호출될 수 있는 정적 함수인 것처럼, 확장 프로퍼티는 프로퍼티처럼 호출할 수 있는 정적 함수이다. <br>
확장 프로퍼티는 값을 저장할 수는 없고, 값을 계산해 돌려줄 수만 있다.

```kt
val Customer.nameForMarketing get() = "${familyName.uppercase()}, $givenName}"
```

<br>

## null이 될 수 있는 수신 객체

> null이 될 수 있는 타입의 확장 함수를 작성한다면 수신 객체가 null일때 null을 반환하는 코드를 작성해선 안된다. <br>
> null을 반환하는 확장 함수가 필요하다면 확장을 null이 될 수 없는 타입으로 확장하고 확장 호출 시 안전한 호출 연산자를 사용하라.

```kt
fun Trip?.reminderAt(currentTime: ZonedDateTime): String = this?.timeUntilDeparture(currentTime)
    ?. toUserFriendlyText()
    ?.plus(" until your next trip!")
    ?: "Start planning your next trip. The world's your oyster!"
```

<br>

## 확장 함수를 메서드로 정의하기

일반적으로 확장 함수는 최상위 함수로 정의한다. <br>
하지만 확장 함수를 클래스 정의 내부에 넣어서, 정의된 클래스의 멤버에 접근할 수 있다.

```kt
class JsonWriter(
    private val objectMapper: ObjectMapper,
) {
    fun Customer.toJson(): JsonNode = objectMapper.valueToTree(this)
}
```

<br>
<br>
<br>

**코드가 함수형 해법을 보여준다면 최상위 함수로 변환하기 쉽고, 최상위 함수는 확장 함수로 변환하기 쉽다.**





