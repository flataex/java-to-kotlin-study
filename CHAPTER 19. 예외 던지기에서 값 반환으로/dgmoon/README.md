# ch19. 예외 던지기에서 값 반환으로

자바에는 체크 예외와 언체크 예외가 존재하지만, 코틀린에서는 체크 예외가 존재하지 않는다.

오류 처리는 매우 중요하지만 어려운 일처럼 여겨진다. 

## 예외 이전의 오류 처리

요즘 대부분의 오류 처리는 예외 기반이지만 그전에는 그렇지 않았다. 다음과 같은 전략을 사용하였다.

- 오류 무시하기 → 영속적 데이터를 오염시키거나 작업을 조용히 실패하는 문제가 있음.
- 그냥 프로그램 종료하기 → 검증된 적절한 전략, 예외를 던지는 것은 프로그램 아니라 프로시저를 종료하도록 한 전략임.
- 특별한 값(-1 등) 반환하기 → 특별한 값의 의미를 알아야 한다.
- 전역 플래그 설정하기 → 특별한 값 반환과 전역 변수 종류 설정 방법을 조합해서 사용
- 상태 코드 반환 하기 → 값을 다른방식으로 반환할 때만 사용 가능
- 특별한 함수 호출 하기 → 오류가 발생하면 오류 함수를 호출하고 오류를 표현할 수 있는 값을 파라미터로 전달

## 예외를 사용한 오류 처리

이전 방법들은 예외를 무시할 수 있는 여지가 있다. 하지만 예외는 연산 수행 시 오류가 발생하면 예외를 명시적으로 처리한다. 호출자가 처리하지 않으면 누군가 처리 할 때까지 예외가 전달된다.

## 자바와 체크 예외

자바에는 예외가 도입되었고, 컴파일러가 예외가 처리되는지 검사해주는 체크 예외가 존재한다.

언체크 예외

- 오류 → 실행 시점에 발생하는 심각한 오류
- 런타임 예외 → 프로그래머의 실수에 따라 발생할 수 있는 문제

체크 예외의 문제점

- 모든 예외를 처리하려면 언체크 예외까지 처리해야 함
- 종류가 너무 다양해서 어떤 예외를 사용해야 하는지 모르게 됨.

## 코틀린과 예외

코틀린의 예외는 체크 예외를 특별 취급하지 않음. 그러나 체크 예외를 고차함수와 함께 사용하기 어렵고 자바 컴파일러 특성을 그대로 물려받아 체크 예외를 무시할 수 있도록 되어 있다.

예외적으로 null 발생 오류를 잘 처리할 것을 알고 오류를 표현하기 위해 null을 사용하기도 한다.

## 예외를 넘어서: 함수형 오류 처리

정적 타입 함수형프로그래밍 언어는 예외 대신 `이더 타입` 기반 오류 처리방식을 선호한다.

함수형 프로그래밍의 `참조 투명성` (식이 참조 투명하면 그 식을 계산한 결과로 안전하게 치환할 수 있다.)

```kotlin
val secondsln24hours = 60 * 60 * 24 // 참조 투명함

```

```kotlin
secondsIn(today()) // 참조 투명하지 않음 -> 언제 이 코드를 호출하느냐에 따라 결과가 달라질 수 있다.
```

참조 투명성을 사용하면 프로그램의 행동 방식을 추론하기 더 쉽고 오류 발생 가능성이 낮아짐.

앞에서 언급한 이더 타입 기반 오류 처리방식은 다음과 같다.

```kotlin
sealed class Either<out L, out R>

data class Left<out L>(val I: L) : Either<L, Nothing>()

data class Right<out R>(val r: R) : Either<Nothing, R>()
```

이더를 오류 처리에 사용하면 관습적으로 Right에 결과를 반환하고 Left를 오류 반환하는데 사용한다.

```kotlin
fun parselnt(s: String): EitherxString, Int> = try {
	Right(lnteger.parselnt(s))
} catch (exception: Exception) {
	Left(exception.message ?: "No message")
}
```

예외 발생 가능한 메서드는 Either를 반환하고 호출자는 성공 값을 벗겨내 사용하거나 실패를 전달한다.

```kotlin
val result: Either<String, Int> = parseInt(readLine() ?: "”)
when (result) {
	is Right -> println("Your number was ${result.r}")
	is Left -> println("I couldn't read your number becase ${result.l}")
}
```

성공 값을 벗겨내 사용하는 것은 귀찮으니 다음과 같이 map을 정의한다

```kotlin
inline fun <L, R1, R2> Either<L, R1>.map(f: (R1) -> R2): EitherxL, R2> =
when (this) {
	is Right -> Right(f(this.r))
	is Left -> this
}
```

## 코틀린의 오류 처리

- 디폴트 전략 → 타입 검사가 부족. 신뢰성 있는 시스템 구축 어려움
- 가장 선호되는 방법은 모든 입력에 대해 결괏값을 제공할 수 없는 연산의 경우 Either를 반환 하는 것임

## 예외를 오류로 리팩터링하기

```kotlin
public class CustomerRegistrationHandler {
	private final IRegisterCustomers registration;
	private final ObjectMapper objectMapper = new ObjectMapper();
	public CustomerRegistrationHandler(IRegisterCustoniers registration) {
		this.registration = registration;
	}

	public Response handle(Request request) {
		try {
			RegistrationData data = objectMapper.readValue(
			request.getBody(), 
			RegistrationData.class
		); // 요청 본문에서 데이터 추출

		Customer customer = registration.register(data); // registration에 데이터 전달
			return new Response(HTTP_CREATED,
			objectMapper.writeValueAsString(customer) // JSON 표현 응답 반환
		);
		} catch (JsonProcessingException x) {
			return new Response(HTTP_BAD_REQUEST); // 오류 상태 코드 반환
		} catch (ExcludedException x) {
			return new Response(HTTP_FORBIDDEN); // 오류 상태 코드 반환
		} catch (DuplicateException x) {
			return new Response(HTTP_CONFLICT); // 오류 상태 코드 반환
		} catch (Exception x) {
			return new Response(HTTP_INTERNAL_ERROR); // 오류 상태 코드 반환
		}
	}
}
```

### 변환전략

책에서는 위 코드를 Result4K를 사용하여 함수형 코드로 전환한다. 저수준에서 시작해 더 이상 체크 예외를 사용하지 않도록 올라가면서 변환한다.

### 맨 밑에서 시작하기

```kotlin
interface Customers {
	@Throws(DuplicateException::class) 
	fun add(name: String, email: String): Customer
	fun find(id: String): Optional<Customer>
}
```

코틀린은 체크 예외를 제공하지 않지만 @Throws 애너테이션을 사용하여 바이트 코드로생성되는 메서드 시그니처에 예외를 추가해서 자바와 상호 운용할 수 있다.

그다음, 인터페이스에 Customers::add의 새 버전(addToo)을 추가하고 Result<Customer, DuplicateException을 반환한다.

```kotlin
interface Customers {
@Throws(DuplicateException::class)
fun add(name: String, email: String): Customer
fun addToo(name:Str:ing, email:String)
	: ResultcCustomer, DuplicateException> =
	try {
		Success(add(name, email))
	} catch (x: DuplicateException) {
		Failure(x)
	}
	fun find(id: tring): Optional<Customer>
}
```

addToo를 호출하는 곳에서 result를 지역변수에 넣고,

```kotlin
@Throws(ExcludedException::class, DuplicateException::class)
override fun register(data: RegistrationData): Customer {
		return if (exclusionList.execlude(data)) {
			throw ExcludedException()
	} else {
		val result: Result<Customer, DuplicateException> =
			customers.addToo(data.name, data.email)
	result
}
```

결괏값이 Success 또는 Failure로 감싸져 있으므로 풀어서 값을 반환한다.

```kotlin
@Throws(ExcludedException::class, DuplicateException::class)
override fun register(data: RegistrationData): Customer {
	return if (exclusionList.exclude(data)) {
		throw ExcludedException()
	} else {
		val result: Result<Customer, DuplicateException> =
			customers.addToo(data.name, data.email)
		when (result) {
		is Success<Customer> ->
			result.value
		is Failure<DuplicateException> ->
			throw result.reason
}
```

Result::orElse 사용, 변수 인라이닝, when에서 중괄호 제거를 수행하면 다음과 같아진다.

```kotlin
@Throws(ExcludedException::class, DuplicateException::class)
override fun register(data: RegistrationData): Customer {
	when {
		exclusionList.exclude(data) -> throw ExcludedException()
		else -> return customers.addToo(data.name, data.email).orThrow()
	}
}
```

### 축소

현재 인터페이스에는 add 구현과 addToo이 남아 있다 그중 add 구현을 제거 하기 위해 addToo를 직접 구현한다.

```kotlin
class CustomerRegistration(
	private val customers: Customers,
	private val exclusionList: ExclusionList
) : IRegisterCustomers {
		@Throws(ExcludedException::class, DuplicateException::class)
		override fun register(data: RegistrationData): Customer {
				while {
					exclusionList.exclude(data) -> throw ExcludedException()
					else -> return customers.add(data.name, data.email).orThrow()
				}
		}
}
```

### 밖으로 나가기

자바에서와는 다르게 Either에서 예외 선언 순서가 중요하다. 

오류를 기존 타입을 사용해 표현하지 않고 새 타입에 대한 맵으로 표현하는 것이 가장 선호되는 방법이다.

```kotlin
fun handle(request: Request): Response =
	try {
	val data = objectMapper.readValue(
		request.body,
		RegistrationData::class.j ava
	)
	registration.registerToo(data)
		.map { value ->
			Response(
					HTTPJZREATED,
					objectMapper.writeValueAsString(value)
				)
			}
	.recover { reason -> reason.toResponse() }
} catch (x: JsonProcessingException) {
	Response(HTTP_BAD_REQUEST)
} catch (x: Exception) {
	Response(HTTP_INTERNAL_ERROR)
}
```

## 더 수정하기

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
override fun registerToo(
		data: RegistrationData
	): Result<Customer, RegistrationProblem> {
		return when {
			exclusionList.exclude(data) -> Failure(Excluded)
			else -> customers.add(data.name, data.email)
			.mapFailure { exception: DuplicateException -> O
			Duplicate(exception.message)
			}
		}
	}
}
```

```kotlin
class CustomerRegistration(
	private val customers： Customers,
	private val exclusionList: ExclusionList
) : IRegisterCustomers {
@Throws(ExcludedException::class, DuplicateException::class)
override fun register(data: RegistrationData): Customer =
	registerToo(data).recover { error -> O
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
		.mapFailure { exception: DuplicateException ->
		Duplicate(exception.message)
		}
	}
}
```
