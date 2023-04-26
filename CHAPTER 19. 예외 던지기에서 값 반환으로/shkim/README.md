# 함수형 오류 처리

함수형 프로그래밍만의 특색있는 기능으로 **참조 투명성**이 있다. <br>
**참조 투명성**이 있으면 프로그램의 행동 방식에 대해 추론하기가 훨씬 쉽고, 오류가 적어지고, 리팩토링하고 최적화할 기회가 많아진다. 

`Integer.parseInt(String)`을 예로 들면, 올바른 입력에 대해 parseInt는 항상 같은 값을 반환한다. (참조 투명 가능) <br>
하지만 String이 정수를 표현하지 않는 경우 parseInt는 예외를 던지고, 우리는 이 호출의 결과를 예외로 대신할 수 없다. (함수의 반환 값이 Int이지만, 예외의 타입은 Exception) <br>

**예외는 참조 투명성을 깬다**

<br>

예외 대신에 오류를 표현하는 특별한 값을 반환한다면, 참조 투명성을 회복할 수 있다. <br>
이를 가능하게 해주는 것이 Either 타입이다.

```kt
sealed class Either<out L, out R>

data class Left<out L>(val I: L) : Either<L, Nothing>()

data class Right<out R>(val r: R) : Either<Nothing, R>()
```

Either는 관습적으로 Right를 결과를 반환하는 데 사용하고, Left를 오류를 반환하는 데 사용한다. <br>
when과 함께라면 다음과 같이 사용 가능하다.

```kt
val result: Either<String, Int> = parseInt(readLine() ?: "") 
when (result) {
    is Right -> println("Your number was ${result.r}")
    is Left -> println("I couldn't read your number because ${result.l}") 
}
```

<br>

when을 사용해 Either를 벗겨 내는 것이 번잡해서, map을 다음과 같이 정의할 수 있다.

```kt
inline fun <L, RI, R2> Either<L, R1>.map(f: (RI) -> R2): Either<L, R2> = 
    when (this) {
        is Right -> Right(f(this.r))
        is Left -> this 
    }
```

<br>

위와 같은 방식을 응용하면 다음과 같은 로직 작성이 가능하다.

Either의 경우 flatMap은 Right에 대해서만 함수를 적용하고, Left인 경우 변경하지 않고 그대로 전달한다. <br>
최종적으로 아래의 코드는 eitherReadLine 실패 시 실패 내용이 들어있는 Left를 반환하고, 그렇지 않으면 doubleString을 반환한다. <br>
doubleString의 결과는 다시 최종 결과인 Int값이 들어간 Right와, 실패를 표현하는 Left를 반환한다.


```kt
inline fun <L, RI, R2> Either<L, R1>.flatMap(
    f: (RI) -> Either<L, R2>
): Either<L, R2> = 
    when (this) {
        is Right -> f(this.r)
        is Left -> this 
    }
```

```kt
fun BufferedReader.eitherReadLine(): Either<String, String> = 
    try {
        val line = this.readLine()
        if (line == nuU)
            Left("No more lines") 
        else
            Right(line)
    } catch (x: IOException) {
        Left(x.message ?: "No message")
    }
```

```kt
fun doubleString(s: String): Either<String, Int> { 
    val result: Either<String, Int> = parselnt(s) 
    return when (result) {
        is Right -> Right(2 * result.r)
        is Left -> result 
    }
}
```

```kt
fun doubleNextLine(reader: BufferedReader): Either<String, Int> =
    reader.eitherReadLine().flatMap { doubleString(it) }
```


