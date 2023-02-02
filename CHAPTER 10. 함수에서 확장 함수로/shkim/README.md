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
    "{{customer.familyName.uppercaseO}, $customer.givenName}"
```

확장성은 `객체지향 < 함수형`

함수형 해법에서는 fullName과 확장 연산을 구분할 수 없지만, 객체지향 해법에서는 메서드와 함수 두가지 형태를 찾아봐야 한다.


