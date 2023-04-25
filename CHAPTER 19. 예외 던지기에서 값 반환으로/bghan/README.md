# 예외 던지기에서 값 반환으로

## 19.1 예외 이전의 오류 처리

#### 과거 예외 처리 기법

- 오류 무시하기 : 호출하는 쪽에서 오류 여부 검사 x
- 프로그램 종료 : 오류 감지하면 프로그램 종료
- 특별한 값 반환 : 오류를 표현하는 특별한 값 반환
- 전역 플래그 설정 : 특별한 값을 반환 하더라도 구체적인 오류 표현이 어려워 이를 해결하기 위해 전역 변수에 오류를 설정하여 특별한 값 반환하여 오류 표현
- 상태 코드 반환 : 오류를 표현하는 상태 코드 반환
- 특별한 함수 호출 : 오류 함수가 반환 값을 통해 실패한 연산을 재시도하거나 중단

## 19.3 자바와 체크 예외

#### 오류

- `java.lang.Error` 하위 클래스는 실행 시점에 JVM의 올바른 동작을 보장할 수 없는 심각한 실패를 위한 예약
- 클래스를 적재할 수 없거나 시스템 메모리 소진한 경우

#### 런타임 예외

- `RuntimeException`
- NPE, Collection index out of range

## 19.5 예외를 넘어서: 함수형 오류 처리

#### 참조 투명성

<table>
<tr>
<td align="center">참조 투명성 O</td><td align="center">참조 투명성 X</td>
</tr>
<tr>
<td>

```kotlin
val secondsIn24hours = 60 * 60 * 24
```
</td>
<td>

```kotlin
secondsIn(today())
```
</td>
</tr>
<tr>
<td>결과에 영향을 끼치지 않고 여러 계산식으로 치환 가능
<td>today() 함수가 실행한 순간마다 서로 다른 결과를 반환</td>
</tr>
</table>

#### 오류 케이스엔 유효하지 않은 참조 투명성

ex) `parseInt`

- `String` 이 정수를 표현하지 않는 경우 `parseInt` 는 결과 반환 대신 예외 던짐
- 함수 반환 타입은 `Int` 지만 예외 타입은 `Exception` &rarr; 참조 투명성 x<br>
&rArr; 특별한 값으로 반환하면 참조 투명성 유지 가능 ex) `Either`

> `Either`는 관습적으로 `Right`를 결과 반환, `Left`를 오류 반환 하는 데 사용

```kotlin
fun parseInt(s: String): Either<String, Int> = try {
    Right(Integer.parseInt(s))
} catch (exception: Exception) {
    Left(exception.message ?: "No message")
}

val result: Either<String, Int> = parseInt(readLine() ?: "")
when (result) {
    is Right -> println("Your number was ${result.r}")
    is Left -> println("I couldn't read your number because ${result.l}")
}
```

## 19.7 예외를 오류로 리팩터링하기

### 19.7.2 맨 밑에서 시작하기

<table>
<tr>
<td align="center">Java</td><td align="center">Kotlin</td>
</tr>
<tr>
<td>

```java
public interface Customers {
    Customer add(String name, String email) throws DuplicateException;
    Optional<Customer> find(String id);
}
```
</td>
<td>

```kotlin
 import kotlin.jvm.Throws

 interface Customers {
    @Throws(DuplicateException::class)
    fun add(name: String, email: String): Customer
    
    fun find(id: String): Optional<Customer>
}
```
</td>
<tr>
</tr>
</table>

- `Customers::add` 에서 예외를 던지는 대신 `Result<Customer, DuplicateException>` 반환하는 함수 추가 정의

```kotlin
 import kotlin.jvm.Throws

interface Customers {
    @Throws(DuplicateException::class)
    fun add(name: String, email: String): Customer
    
    fun addToo(name: String, email: String): Result<Customer, DuplicateException> = 
        try {
            Success(add(name, email))
        } catch (x: DuplicateException) {
            Failure(x)
        }

    fun find(id: String): Optional<Customer>
}
```

<table>
<tr>
<td align="center">AS-IS</td><td align="center">TO-BE</td>
</tr>
<tr>
<td>

```kotlin
 import kotlin.jvm.Throws

@Throws(ExcludedException::class, DuplicateException::class)
override fun register(data: RegistrationData): Customer {
    return if (exclusionList.exclude(data)) {
        throw ExcludedException()
    } else {
        val result = customers.add(data.name, data.email)
        result
    }
}
```
</td>
<td>

```kotlin
import kotlin.jvm.Throws

@Throws(ExcludedException::class, DuplicateException::class)
override fun register(data: RegistrationData): Customer {
    return if (exclusionList.exclude(data)) {
        throw ExcludedException()
    } else {
        val result: Result<Customer, DuplicateException> = 
            customers.addToo(data.name, data.email)
        
        when (result) {
            is Success<Customer> -> result.value
            is Failure<DuplicateException> -> throw result.reason
        }
    }
}
```
</td>
</tr>
<tr>
<td>

```kotlin
import kotlin.jvm.Throws

@Throws(ExcludedException::class, DuplicateException::class)
override fun register(data: RegistrationData): Customer {
    return if (exclusionList.exclude(data)) {
        throw ExcludedException()
    } else {
        customers.addToo(data.name, data.email).orThrow()
    }
}
```
</td>
<td>

```kotlin
import kotlin.jvm.Throws

@Throws(ExcludedException::class, DuplicateException::class)
override fun register(data: RegistrationData): Customer {
    when {
        exclusoinList.exclude(data) -> throw ExcludedException()
        else -> return customers.addToo(data.name, data.email).orThrow()
    }
}
```
</td>
</tr>
</table>

## 19.8 더 수정하기

<table>
<tr>
<td align="center">AS-IS</td><td align="center">TO-BE</td>
</tr>
<tr>
<td>

```kotlin
class CustomerRegistration(
    private val customers: Customers,
    private val exclusionList: ExclusionList
) : IRegisterCustomers {
    
    @Throws(ExcludedException::class, DuplicateException::class)
    override fun register(data: RegistrationData): Customer {
        when {
            exclusionList.exclude(data) -> throw ExcludedExcepion()
            else -> return customers.add(data.name, data.email).orThrow()
        }
    }
    
    override fun registerToo(
        data: RegistrationData
    ): Result<Customer, RegistrationProblem> {
        return when {
            exclusionList.exclude(data) -> Failure(Excluded)
            else -> customers.add(data.name, data.email)
                .mapFailure{ exception: DuplicateException ->
                    Duplicate(exception.message)
                }
        }
    }
}
```
</td>
<td>

```kotlin
class CustomerRegistration(
    private val customers: Customers,
    private val exclusionList: ExclusionList
) : IRegisterCustomers {
    
    @Throws(ExcludedException::class, DuplicateException::class)
    override fun register(data: RegistrationData): Customer = 
        registerToo(data).recover { error ->
            when (error) {
                is Excluded -> throw ExcludedException()
                is Duplicate -> throw DuplicateException(error.message)
            }
        }
    
    override fun registerToo(
        data: RegistrationData
    ): Result<Customer, RegistrationProblem> {
        return when {
            exclusionList.exclude(data) -> Failure(Excluded)
            else -> customers.add(data.name, data.email)
                .mapFailure{ exception: DuplicateException ->
                    Duplicate(exception.message)
                }
        }
    }
}
```
</td>
</tr>
</table>

## 19.9 계층

#### `Customers::add` 가 실패할 수 있는 케이스를 `sealed class` 를 통해 정의

```kotlin
interface Customers {
    fun add(name: String, email: String): Result<Customer, CustomersProblem>
}

sealed class CustomerProblem

data class DuplicateCustomerProblem(val message: String): CustomerProblem()
data class DatabaseCustomerProblem(val message: String): CustomerProblem()
```

- 실패 타입이 `CustomerProblem` 기반 클래스이기에 더이상 컴파일 x
&rArr; `CustomerProblem` 클래스에서 `RegistrationProblem` 클래스로 변환

```kotlin
sealed class RegistrationProblem

object Excluded: RegistrationProblem()

data class Duplicate(val message: String): RegistrationProblem()
data class DatabaseProblem(val message: String): RegistrationProblem()
```

- `Customers::add` 가 실패할 수 있는 케이스에서 `CustomerRegistration::register` 가 실패할 수 있는 케이스로 변환

```kotlin
override fun register(
    data: RegistrationData
): Result<Customer, RegistrationProblem> {
    return when {
        exclusionList.exclude(data) -> Failure(Excluded)
        else -> customers.add(data.name, data.email)
            .mapFailure { problem: CustomersProblem ->
                when (problem) {
                    is DuplicateCustomerProblem ->
                        Duplicate(problem.message)
                    is DatabaseCustomerProblem ->
                        DatabaseProblem(problem.message)
                }
            }
    }
}
```

```kotlin
private fun RegistrationProblem.toResponse() = when (this) {
    is Duplicate -> Response(HTTP_CONFLICT)
    is Excluded -> Response(HTTP_FORBIDDEN)
    is DatabaseProblem -> Response(HTTP_INTERNAL_ERROR)
}
```