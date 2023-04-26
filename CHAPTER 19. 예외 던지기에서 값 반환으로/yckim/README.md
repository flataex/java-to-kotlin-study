# 예외 던지기에서 값 반환으로

- 시스템의 초기단계에서는 오류에 대한 처리가 크게 중요하지 않을 수도 있지만 규모가 커질 수록 오류에 대한 처리가 중요해집니다.
    - 자바에서는 체크 예외와 언체크 예외를 사용해 오류를 표현하고 처리합니다.
- 오류를 잘 관리하지 않으면 시스템에 오류가 발생하면 팀의 모든 시간을 잡아먹을 수 있을 정도로 해결하기 어려울 수 있습니다.
    - 오류를 관리했다고 하더라도 오류를 감지하는 코드와 오류를 복구하는 코드의 위치가 멀다면 복구전략을 알기도 어렵고 테스트하기도 힘듭니다.

## 예외 이전의 오류처리
예외를 처리하는 방식 이외에 다음과 같은 방식으로 예외들을 처리했었습니다.

- 오류 무시하기
    - 오류에 대한 처리를 하지 않는 방법입니다.
    - 이런 방식은 작업이 조용하게 실패하기 때문에 별도의 방안이 필요할 수 있습니다.
- 그냥 프로그램 종료하기
    - 오류를 감지하면 바로 프로그램을 종료합니다.
    - 영속적인 데이터의 오염을 방지하기 위해 오류가 발생하면 프로그램을 종료합니다.
- 특별한 값 반환하기
    - 오류를 표현하는 특별한 값을 반환하는 방식입니다.
    - 예를 들어 값이 정상적인 값이 아닐 경우 -1과 같은 값을 반환하는 방식입니다.
    - 하지만 반환 값이 모두 올바른 값이 될 수 있는 함수의 경우에는 이러한 방법을 쓸 수 없고 함수를 호출하는 쪽에서 에러 값에 대한 정보를 알아야 한다는 단점이 있습니다.
    - 특별한 값을 반환할때 오류시 null을 반환하는 방법도 있는데 대부분의 언어에서는 위험하지만 코틀린에서는 널 가능성을 처리하도록 강제할 수 있기 때문에 오히려 안전하고 효과적인 방법이 될 수 있습니다.
- 전역 플래그 설정하기
    - 에러 발생시 특별한 값을 반환하고 전역 변수에 오류의 종류를 설정하는 방법을 조합해 사용하는 방식입니다.
    - 예를 들어 오류를 나타내는 특별한 값이 반환되는 경우 전역 변수의 errno를 검사해서 문제가 무엇인지 알 수 있습니다.
    - 이 기법은 C에서 자주 사용되던 방식입니다.
- 상태 코드 반환하기
    - 함수의 반환 값으로 상태 코드를 반환하는 방법입니다.
    - 함수가 아무값도 반환하지 않거나 전달받은 파라미터 값을 변경하는 식으로 반환할 때 이러한 방법을 사용할 수 있습니다.
- 특별한 함수 호출하기
    - 오류가 발생하면 특별한 함수를 호출하는 방식입니다.
    - 보통은 호출되는 함수의 파리미터로 오류 함수가 전달됩니다.
    - 이 기법은 전략 패턴을 오류처리에 적용한 예로, 예외를 사용할 수 있다고 할지라도 몇몇 틈새 상황에서는 이 기법이 유용한 도구입니다.
## 예외를 사용한 오류 처리
- 앞 절에서 살펴본 모든 기법에는 호출하는 코드가 발생한 예외를 무시할 수 있는 여지가 있습니다.
- 예외는 문제 해결을 강제할 수 있기 때문에 사용자가 예외를 처리하지 않는 상황을 막을 수 있습니다.
## 자바와 체크 예외
- 자바는 오류를 제어하기 위해 오류와 체크예외와 언체크예외를 통해 오류 상황을 파악합니다.
    - 오류는 JVM의 올바른 동작을 보장할 수 없는 심각한 실패상황을 위해 예약되어 있으며 따로 처리하지 않게 됩니다.
    - 체크 예외는 프로그래머가 제어할 수 없는 상황에서 실패에서 복구하는 방법을 알고 있는 경우 사용됩니다.
        - 에러를 외부로 던지거나 try catch 문을 통해 예외를 복구해야 합니다.
    - 언체크 예외는 프로그래머의 실수에 따라 발생한 문제에 대해서 사용됩니다.
        - 에러 처리가 강제되지 않습니다. (처리하지 않을 경우 작업이 종료됩니다.)
- 자바 초기에는 체크 예외를 선호했었지만 세월이 지나면서 다음과 같은 문제가 부각되었습니다.
    - Exception의 하위 클래스로 RuntimeException을 가지기 때문에 모든 체크 예외를 처리하기 원하는 코드가 언체크 예외까지 처리하는 문제가 발생합니다.
    - 자바 API가 체크 예외와 언체크 예외를 일관성 있게 처리하지 않습니다.
    - 자바 8의 람다식에서 체크 예외를 전파할 수 없습니다.
## 코틀린과 예외
- JVM에서 실행되기 때문에 코틀린도 예외가 존재하며, 언어 플랫폼에 예외가 내장되어 있습니다.
    - 하지만 코틀린은 체크 예외를 특별하게 취급하지 않습니다.
    - 자바도 거의 체크 예외를 사용하지 않게 되었으며, 고차 함수의 경우 체크 예외를 사용하기 어렵기 때문에 거의 사용하지 않습니다.
- 체크 예외가 JVM의 특성이 아니고 자바 컴파일러의 특성이기 때문에 코틀린은 상당수의 체크예외를 무시할 수 있습니다.
    - 컴파일러는 바이트코드에 메서드가 발생시킬 수 있는 체크 예외를 기록하지 않을 수 있는데, JVM 자체는 이에 대해 신경쓰지 않습니다.
- 코틀린에서는 함수를 호출하는 쪽에서 null 가능성을 염두하고 오류를 표현하기 위해 null을 사용하기 좋습니다.
    - 이런 예시로 `<T> Iterable<T>.firstOrNull(): T?`를 예로 들 수 있습니다.
## 예외를 넘어서 : 함수형 오류 처리
- 참조 투명성이란 어떤 식이 참조 투명하면 그 식을 계산한 결과로 안전하게 치환할 수 있는 것을 의미합니다.

    ```kotlin
    // 참조 투명한 변수
    val secondsIn24Hours = 60 * 60 * 24
    
    // 참조 투명하지 않은 함수
    secondsIn(today())
    ```

- 참조 투명성이 있으면 프로그램의 행동 방식에 대해 추론하기가 훨씬 쉽기 때문에 참조 투명성을 보장하기 위해 노력하는 것이 좋습니다.
- 예외를 던지는 경우 Exception 타입이 반환될 수 있기 때문에 해당 함수는 참조 투명하지 않습니다.
- 코틀린에서는 널 가능성을 통해 참조 투명성을 달성할 수 있습니다.
    - 하지만 널만 반환해서는 왜 예외가 발생했는지에 대한 상황을 파악할 수 없습니다.
- 이더 타입을 사용하면 값을 반환하거나 오류가 발생한 상황에서 오류 값을 전달할 수 있습니다.
    - 이더 타입은 두 타입을 저장할 수 있지만, 어느 한 순간에는 한 가지 값만 저장할 수 있습니다.

    ```kotlin
    sealed class Either<out L, out R>
    
    data class Left<out L>(val l: L) : Either<L, Nothing>()
    
    data class Right<out R>(val r: R) : Either<Nothing, R>()
    ```

- 이더 타입을 사용하면 다음과 같이 예외 대신에 에러 메시지를 반환해줄 수 있으며 when 식과 스마트 캐스트를 통해 깔끔한 코드를 작성할 수 있습니다.

    ```kotlin
    
    fun parseInt(s: String): Either<String, Int> = try {
    	Right(Integer.praseInt(s))
    } catch (exception: Exception) {
    	Left(exception.message ?: "No message")
    }
    ```

    ```kotlin
    // 호출 예시
    val result: Either<String, Int> parseInt(readLine() ?: "")
    
    when (result) {
    	is Right -> println("Your number was ${result.r}")
    	is Left -> println("I couldn't read your number because ${result.l}")
    }
    ```

    - Either 타입을 반환하여 when식과 스마트 캐스트를 이용하여 클라이언트가 실패하는 경우를 반드시 처리할 수 있습니다.
    - 이를 통해 체크 예외의 장점을 살릴 수 있습니다.
    - 호출자는 이를 응용하여 값을 래핑하는 함수들을 만들 수 있습니다.

        ```kotlin
        fun doubleString(s: String): Either<String, Int> {
        	val result: Either<String, Int> = parseInt(s)
        	return when (result) {
        		is Right -> Right(2 * result.r)
        		is Left -> result
        	}
        }
        ```

- when 식과 스마트 캐스트를 이용하는 Either를 다음과 같이 템플릿으로 만들어 사용할 수 있습니다.

    ```kotlin
    inline fun <L, R1, R2> Either<L, R1>.map(f: (R1) -> R2): Either<L, R2> =
    	when (this) {
    		is Right -> Right(f(this.r))
    		if Left -> this
    	} 
    ```

    ```kotlin
    // 적용 예시
    fun doubleString(s: String): Either<String, Int> = parseInt(s).map { 2 * it }
    ```

- 다음과 같이 Either를 반환하는 래퍼 함수를 만들어 사용할수도 있습니다.

    ```kotlin
    inline fun<L, R1, R2> Either<L, R1>.flatMap(
    	f: (R1) -> Either<L, R2>
    ): Either<L, R2> =
    	when (this) {
    	is Right -> f(this.r)
    	is Left -> this
    }
    ```

    ```kotlin
    fun BufferedReader.eitherReadLine(): Either<String, String> = 
    	try {
    		val line = this.readLine()
    		if (line == null)
    			Left("No more lines")
    		else
    			Right(line)
    	} catch (x: IOExcetion) {
    		Left(x.message ?: "No message")
    	}
    ```

    ```kotlin
    fun doubleNextLine(reader: BufferedReader): Either<String, Int> =
    	reader.eitherReadLine().flatMap { doubleString(it) }
    ```

- 위의 예시들 처럼 map과 flatmap을 연쇄적으로 호출하는 과정을 통해 예외를 던질 수 있는 것처럼 동작하게 할 수 있습니다.
## 코틀린의 오류 처리
- 코틀린에서 오류 처리는 상황에 따라 다릅니다.
    - 실패 이유와 관련한 정보를 전달할 필요가 없는 경우에는 널이 될 수 있는 타입을 사용해 실패를 표현하는 방법이 효과적입니다.
    - 예외를 사용할 수도 있지만 다음과 같은 문제가 존재합니다.
        - 타입 검사가 부족해지므로 예외를 사용하면 어떤 코드로 어떤 실패가 발생할지에 대해 서로 의사소통하기 어려워지고 그로인해 신뢰할 수 있는 시스템을 구축하기 어려워집니다.
        - 참조 투명성이 주는 이점도 사라지기 때문에 신뢰하기 어려운 시스템이 만들어질 수 있습니다.
- 자바에서 I/O 문제 등으로 검사 예외를 던질법한 연산이나 parseInt 처럼 모든 입력에 대해 결과값을 제공할 수 없는 연산의 경우에는 Either를 반환하는 것도 좋습니다.
    - 이 방법을 사용하면 예외를 더 치명적인 오류를 위해 남겨둘 수 있습니다.
- Either 타입으로 사용하는 타입으로 내장 코틀린의 Result 타입은 코루틴에 적합하게 설계되었으며 예외로만 실패값을 나타낼 수 있기 때문에 추천하지 않습니다.
    - 책에서는 Result4K라는 라이브러리를 추천합니다.
## 예외를 오류로 리팩터링하기
1. 다음과 같은 예외를 던지는 코틀린 인터페이스가 있다고 가정해봅시다.

    ```java
    interface Customers {
        @Throws(DuplicateException::class) // 1
        fun add(name: String, email: String): Customer
        fun find(id: String?): Optional<Customer>
    }
    ```

2. 이더 타입을 이용하여 반환하는 addToo 메서드를 추가합시다.

    ```java
    fun addToo(name: String, email: String): Result<Customer, DuplicateException> =
            try {
                Success(add(name, email))
            } catch (x: DuplicateException) {
                Failure(x)
            }
    ```

3. 다음은 add를 addToo로 변환할 코틀린 코드입니다.

    ```java
    class CustomerRegistration(
        private val customers: Customers,
        private val exclusionList: ExclusionList
    ) : IRegisterCustomers {
        @Throws(ExcludedException::class, DuplicateException::class)
        override fun register(data: RegistrationData): Customer {
            return if (exclusionList.exclude(data)) {
                throw ExcludedException()
            } else {
                customers.add(data.name, data.email)
            }
        }
    }
    ```

4. addToo로 변환합니다.

    ```kotlin
    class CustomerRegistration(
        private val customers: Customers,
        private val exclusionList: ExclusionList
    ) : IRegisterCustomers {
        @Throws(ExcludedException::class, DuplicateException::class)
        override fun register(data: RegistrationData): Customer {
            return if (exclusionList.exclude(data)) {
                throw ExcludedException()
            } else {
                val result: Result<Customer, DuplicateException> 
                  = customers.addToo(data.name, data.email) // 1
                result
            }
        }
    }
    ```

5. add를 다시 사용하도록 변경합니다.

    ```kotlin
    class CustomerRegistration(
        private val customers: Customers,
        private val exclusionList: ExclusionList
    ) : IRegisterCustomers {
        @Throws(ExcludedException::class, DuplicateException::class)
        override fun register(data: RegistrationData): Customer {
            when {
                exclusionList.exclude(data) -> throw ExcludedException()
                else -> return customers.add(data.name, data.email).orThrow()
            }
        }
    }
    ```

- Result 타입으로 반환하기 때문에 발생할 수 있는 예외에 대해 좀 더 명확하게 알 수 있습니다.
## 더 수정하기
- 리팩터링을 통해 예외를 던지지 않고 Failure 값에 들어가기 때문에 예외가 다른 클래스에 전파되지 않고 오류를 감지할 수 있습니다.
- 별도의 Duplicate 클래스를 만들기 때문에 Exception을 확장하지 않고 별도의 Duplicate 클래스를 사용할 수 있습니다.
## 계층
- 한 결과안에 오류 타입을 여러개를 표현하지 않기 위해 각 도메인 별로 봉인된 클래스를 만들고 하위 클래스로 어떤 오류인지 표현하는 하위 클래스들을 만들어 표현할 수 있습니다.
    - 봉인된 클래스와 when 타입 검사를 이용하므로 안전하게 코드를 작성할 수 있다.
## 정리
- 체크 예외에 의존하는 자바 코드를 코틀린으로 변환하고 싶은 경우 이더 타입을 활용하면 깔끔하게 코드를 작성할 수 있습니다.
- 봉인된 클래스를 사용해 오류 상황을 열거할 수 있지만 여러 계층에 같은 오류 타입을 전파할수는 없게 됩니다.
    - 하지만 when 절을 이용하여 해당 오류를 체크하지 않고 넘어가지 못하게 막을 수 있기 때문에 안전합니다.
