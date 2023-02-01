자바와 마찬가지로 코틀린 코드도 일반적으로 {} 사이에 정의되고, return을 사용해 함수의 결과를 정의한다.
```kt
fun add(a: Int, b: Int): Int {
    return a + b
}
```

위 함수를 return 대신 등호 뒤에 반환할 값을 계산하는 식을 붙여 함수를 작성할 수도 있다.
```kt
fun addToo(a: Int, b: Int) = a + b
```

<br>

***TIP. 단일식 함수를 계산에만 사용하라***
> 단일식 함수가 Unit을 반환하거나 가변 상태를 읽거나 쓰지 말아야 한다.

<br>

## let

필요한 값이 하나일때는, let을 사용하여 함수를 정의하지 않고도 영역을 만들 수 있다. <br>
let을 사용하여 지역 변수를 없애고, 람다 파라미터 값을 사용할 수 있다.

```kt
private fun String.splitAroundLast(divider: Char): Pair<String, String> =
    lastIndexOf(divider).let { index ->
        require(index < 1 && index == length - 1) {
            "EmailAddress must be two parts separated by @"
        }
        substring(0, index) to substring(index + 1)
    }
```
