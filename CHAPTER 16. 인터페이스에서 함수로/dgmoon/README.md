## ch.16 인터페이스에서 함수로

```kotlin
data class Email(
	val to: EmailAddress,
	val from: EmailAddress,
	val subject: String,
	val body: String
) {
	fun sendEmail(
		email: Email,
		serverAddress: InetAddress,
		username: String,
		password: String
		) {
	}
}
```

전자 우편 보내기 클래스에서 전자우편을 보내기위해서는 이메일 주소 뿐만 아니라 서버 주소, 사용자 계정 및 비밀번호 등이 필요 할 수 있다. 실제 사용자는 몰라도 되지만 이메일을 보내기 위해서는 필요한 이 정보들을 감추는 방법에는 몇 가지가 있다.

### 객체지향 캡슐화

```kotlin
class EmailSender(
	private val serverAddress: InetAddress,
	private val username: String,
	private val password: String
) {
	fun send(email: Email) {
		sendEmail(
			email,
			serverAddress,
			username,
			password
		)
	}
}
```

EmailSender 객체를 만들고 데이터를 캡슐화 하였다. 이 클래스의 필드가 이미 세부 데이터를 저장하고 있으므로 메서드가 새로 세부 데이터를 받을 필요가 없어졌다.

```kotlin
// 설정을 알 수 있는 곳
val sender: EmailSender = EmailSender(
	inetAddress("smtp.travelator•com"),
	"username",
	"password"
)
// 메세지를 보내는 곳
fun sendThanks() {
	sender.send(
		Email(
		to = parse("supportQinternationalrescue.org"),
		from = parse("supportQtravelator.com"),
		subject = "Thanks for your help",
		body = "..."
		)
	)
}
```

일반적으로 Email에 대한 설정을 알 수 있는 곳과 실제 생성된 Email 객체를 사용해 메시지를 보내는 곳이 다르다.

여기서 인터페이스를 추출하여 사용하기도 한다. 클라이언트 코드가 EmailSender가 아닌 ISendEmail에 의존하면 테스트가 ISendEmail을 사용하여 실제로 이메일을보내지 않고도 보낸 것처럼 테스트 할 수 있게 해준다.

```kotlin
fun interface ISendEmail {
		fun send(email: Email)
	}

	fun createEmailSender(
		serverAddress: InetAddress,
		username: String,
		password: String
	) = ISendEmail { email ->
		sendEmail(
			email,
			serverAddress,
			username,
			password
		) // 여기서 반환한 객체를 "클로저"라고 한다.(람다식 적용됨)
}
```

ISendEmail이라는 클래스를 만들지 않더라도 위와 같이 익명으로 구현할 수 도 있다. 이렇게 하면 클라이언트가 특정 구현 클래스에 의존해서 ISendMail 객체를 그 클래스로 다운 캐스팅 한 후 다른 메서드를 호출하는 등의 조작이 불가능하다. 

### 함수형 캡슐화

Email 객체가 이메일을 보내기 위해 세부 정보가 필요한 것을 부분 적용(partial application)이라고 한다. `부분 적용은 함수의 인자 중 일부를 고정시키면서 인자가 더 적은 새 함수를 만들어내는 기법`이다.

```kotlin
fun createEmailSender(
	serverAddress: InetAddress,
	username: String,
	password: String
): (Email) -> Unit {
	return (Email) -> Unit =
		{ email ->
			sendEmail(
			email,
			serverAddress,
			username,
			password
			)
		}
}
```

createEmailSender는 생성자이고 반환타입이 Email을 받아서 Unit을 반환 하는 함수이다. createEmailSender는 객체 지향 캡슐화와 마찬가지로 한 장소에서 함수를 만들고 다른 곳에서 그 함수를 사용한다.

```kotlin
// 설정을 알 수 있는 곳
val sender: (Email) -> Unit = createEmailSender(
	inetAddress("smtp.travelator.com”),
	"username",
	"password"
)

// 메시지를 보내는 곳
fun sendThanks() {
	sender(
		Email(
		to = parse("support@internationalrescue.org"),
		from = parse("support@travelator.com"),
		subject = "Thanks for your help",
		body = "..."
		)
	)
}
```

### 자바의 함수 타입

자바 8에서 람다 도입 시, 함수 타입을 가리키기위해 자바는 원하는 시그니처의 단일 추상 메서드(SAM) 인터페이스(Consumer, Supplier. Function. BiFunction, Predicate 등)를 사용한다.

```java
// 설정을 알 수 있는 곳
Consumer<Email> sender = createEmailSender(
	inetAddress("example.com"),
	"username",
	"password"
);

// 메시지를 보내는 곳
public void sendThanks() {
	sender.accept( //accept는 Consumer 인터페이스에 정의된 유일한 추상 메서드의 이름
		new Email(
			parse("support@internationalrescue.org"),
			parse("support@travelator.com"),
			"Thanks for your help".
			"..."
		)
	);
}
```

createEmailSender를 람다로 구현하면 다음과 같다.

```java
static Consumer<Email> createEmailSender(
	InetAddress serverAddress,
	String username,
	String password
) {
	return email -> sendEmail(
		email,
		serverAddress,
		username,
		password
	)；
}
```

코틀린의 createEmailSender가 반환하는 `(Email) -> Unit` 타입과 자바의 `Consumer<Email>` 타입은 서로 호환되지 않기 때문에 혼합 사용 시에는 한쪽을 감싸야 한다.

```kotlin
val sender： (Email) -> Unit = createEmailSender(
	inetAddress("smtp.travelator.com"),
	"username",
	"password"
)

val consumer: Consumer<Email> = sender // 컴파일 불가
// sender를 그냥 Consumer<Email>에 대입 할 수 없기 때문에 다음과 같이 람다로 변환하여 사용

val consumer： Consumer<Email> = Consumer<Email> { email ->
	sender(email)
}
```

### 믹스 앤드 매치

객체 지향과 함수형 해법을 하나로 합쳐서, 객체 지향 구현을 받는 클라이언트에게 함수를 넘기는, 다시 말해, ISendEmail을 (Email) → Unit 타입으로 변환하거나 반대 방향으로 변환할 수 있다.

Consumer<Email>나 (Email) → Unit 각 타입에 적절한 시그니처의 메서드를 정의함으로써 각각을 구현할 수 있다.

- 자바와 같이 메서드 참조 사용 가능
    
    ```kotlin
    val sender： (Email) -> Unit = instance::send
    ```
    
- fun interface로 좀 더 간략하게 익명 객체 작성 가능
    
    ```kotlin
    fun interface ISendEmail {
    	fun send(email: Email)
    }
    
    val sender = ISendEmail { function(it) }
    ```
    

### 결합

객체지향 캡슐화는 함수형 캡슐화보다 결합도가 높아진다.

객체 지향 캡슐화는 클라이언트가 인터페이스를 정의하지 못하고 다른 어딘가에 정의되어 있어야 하기 때문에 클라이언트와 구현이 인터페이스에 따라 결합되게 되고 시스템에대한 리팩터링과 추론이 힘들어진다. 하지만 함수형 캡슐화에서는 런타임이 모든 함수 타입을 정의하므로 컴파일 시점에 의존성이 발생하지 않는다.

### 객체지향인가 함수형인가

두 접근법 모두 같은 목적을 달성할 수 있고 표현력도 비슷하지만, 더 큰 인터페이스에 의존하면 필요한 연산이 무엇인지 모호해지고 모든 인터페이스를 구현하게 된다. 따라서 함수 타입으로 표현하는쪽을 기본 값으로 택하고 필요에 따라서 클래스로 전환해야 한다.

### 자바의 레거시

자바 8이 도입되기 전에는 런타임이 모든 함수 타입을 정의하였다. 하위 시스템으로 의존 관계를 묶고 싶을 때 다중메서드 인터페이스를 사용해 의존관계를 표현하였다. 이로 인해 결합도가 높아진다는 문제가 발생하였고 목 프레임워크가 실제로 하나의 메서드만 호출되는 경우에도 더 넓은 인터페이스의 테스트 구현을 만들어야 하는 문제점이 있었다.

### 추적 가능성

의존 관계를 함수 타입으로 표현하면(EmailSystem：:send가 （Email） -> Unit이 되면) 추적하기 어려워진다. 하지만 결합을 없애고 일반성을 얻기 위한 비용이며 추후 IDE 지원이 개선되리라 예상ㅎ

함수형 프로그래밍에서는 객체지향과 다르게 ‘설정’과 ‘구현’을 클래스 안에 감추고 치환할 수 있는 기능을 둘 다 함수가 담당한다.

코틀린에서는 객체를 함수의 컬렉션으로 볼 수도 있고, 함수를 메서드가 하나인 객체로 볼 수도 있다. 하지만 코틀린은 함수를 사용하면  인터페이스 사용 시보다 결합을 줄일 수 있기 때문에 인터페이스보다 함수 타입 활용을 장려한다.
